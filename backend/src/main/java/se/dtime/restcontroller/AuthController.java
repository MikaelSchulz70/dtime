package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import se.dtime.config.AuthentikEndSessionUriResolver;
import se.dtime.config.OidcSwitchUserRedirectSupport;
import se.dtime.config.PublicFrontendUrlResolver;
import se.dtime.config.SwitchUserIntentSupport;
import se.dtime.config.SwitchUserOAuth2AuthorizationRequestResolver;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    static final String SWITCH_USER_CONTINUE_PATH = "/api/auth/oidc/switch-user/continue";

    static final String SWITCH_USER_RESUME_PATH = "/api/auth/oidc/switch-user/resume";

    @Value("${oauth.authentik.enabled:false}")
    private boolean authentikOAuthEnabled;

    @Value("${oauth.authentik.end-session-uri:}")
    private String authentikEndSessionUri;

    /**
     * Browser-facing backend origin for OAuth start after IdP logout (must match Authentik allowlist).
     */
    @Value("${oauth.authentik.backend-public-base-url:}")
    private String backendPublicBaseUrl;

    /**
     * Optional override for the public SPA origin during switch-user continue fallback.
     */
    @Value("${oauth.authentik.switch-user-return-base-url:}")
    private String switchUserReturnBaseUrl;

    @Value("${app.frontend.dev-server.url:}")
    private String frontendDevServerUrl;

    @Value("${app.frontend.dev-server.enabled:false}")
    private boolean devServerEnabled;

    @Value("${oauth.authentik.switch-user-idp-logout:false}")
    private boolean switchUserIdpLogout;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    private final TokenBasedRememberMeServices rememberMeServices;

    public AuthController(@Autowired(required = false) TokenBasedRememberMeServices rememberMeServices) {
        this.rememberMeServices = rememberMeServices;
    }

    @GetMapping("/oidc/status")
    public Map<String, Object> getOidcAuthStatus() {
        Map<String, Object> response = new HashMap<>();

        boolean isAvailable = authentikOAuthEnabled && clientRegistrationRepository != null;

        response.put("enabled", isAvailable);
        return response;
    }

    @GetMapping("/oidc/failure")
    public ResponseEntity<Map<String, Object>> getOidcFailure(@RequestParam(value = "reason", required = false) String reason) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "oauth");
        response.put("reason", reason == null ? "oauth_failure" : reason);
        response.put("message", "OIDC login failed.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Clears the DTime session, logs out at Authentik, then returns to OAuth authorize with {@code prompt=login}.
     * Post-logout redirect targets the backend OAuth start URL ({@code :8443}) so Authentik completes the
     * DTime authorization flow instead of dropping the user on the application library.
     */
    @GetMapping("/oidc/switch-user")
    public void switchOidcUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("OIDC switch-user: clearing application session");
        clearApplicationSession(request, response);
        SwitchUserIntentSupport.markSwitchUserIntent(request, response);

        String oauthRestartUrl = resolveOAuthAuthorizationStartAbsoluteUrl(request);
        if (!authentikOAuthEnabled) {
            response.sendRedirect(oauthRestartUrl);
            return;
        }

        String endSessionEndpoint = resolveAuthentikEndSessionUrl();
        String postLogoutRedirectUrl = resolveSwitchUserPostLogoutAbsoluteUrl(request);
        if (switchUserIdpLogout
                && StringUtils.hasText(endSessionEndpoint)
                && StringUtils.hasText(postLogoutRedirectUrl)
                && clientRegistrationRepository != null) {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");
            if (registration != null) {
                // Use a query-string-free continue URL — Authentik strict redirect URIs often reject
                // post_logout targets like /oauth2/authorization/authentik?switch_user=1 and fall back to /.
                String idpLogout = UriComponentsBuilder.fromUriString(endSessionEndpoint)
                        .queryParam("client_id", registration.getClientId())
                        .queryParam("post_logout_redirect_uri", postLogoutRedirectUrl)
                        .encode()
                        .build()
                        .toUriString();
                log.info("OIDC switch-user: redirecting to IdP end-session, post_logout_redirect_uri={}",
                        postLogoutRedirectUrl);
                response.sendRedirect(idpLogout);
                return;
            }
        }

        log.info("OIDC switch-user: starting OAuth without IdP end-session, target={}", oauthRestartUrl);
        startOAuthWithSwitchUserFlag(request, response, oauthRestartUrl);
    }

    /**
     * Continues OAuth when switch-user was started but the browser returned to the login page
     * (for example Authentik end-session did not reach {@link #switchOidcUserContinue}).
     */
    @GetMapping("/oidc/switch-user/resume")
    public void switchOidcUserResume(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("OIDC switch-user resume: restarting OAuth after IdP logout or interrupted flow");
        SwitchUserIntentSupport.markSwitchUserIntentAfterIdpLogout(request, response);
        String oauthStartUrl = resolveOAuthAuthorizationStartAbsoluteUrl(request);
        log.info("OIDC switch-user resume: OAuth start target={}", oauthStartUrl);
        startOAuthWithSwitchUserFlag(request, response, oauthStartUrl);
    }

    /**
     * Fallback when Authentik returns to the SPA continue URL instead of the backend OAuth start URL.
     */
    @GetMapping("/oidc/switch-user/continue")
    public void switchOidcUserContinue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switchOidcUserResume(request, response);
    }

    private void clearApplicationSession(HttpServletRequest request, HttpServletResponse response) {
        if (rememberMeServices != null) {
            rememberMeServices.logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
        }
        SecurityContextHolder.clearContext();

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
    }

    private void startOAuthWithSwitchUserFlag(HttpServletRequest request, HttpServletResponse response, String oauthStartUrl)
            throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);
        response.sendRedirect(oauthStartUrl);
    }

    /**
     * Post-logout redirect after Authentik end-session (no query string — must match Authentik allowlist).
     * Prefer backend :8443 because switch-user starts there; SPA URL is a dev fallback.
     */
    private String resolveSwitchUserPostLogoutAbsoluteUrl(HttpServletRequest request) {
        String backendBase = SwitchUserIntentSupport.resolveBackendPublicBaseUrl(
                backendPublicBaseUrl, devServerEnabled, frontendDevServerUrl);
        if (StringUtils.hasText(backendBase)) {
            return backendBase + SWITCH_USER_RESUME_PATH;
        }
        if (devServerEnabled) {
            String spaBase = resolveSpaPublicBaseUrl(request);
            if (StringUtils.hasText(spaBase)) {
                return spaBase + SWITCH_USER_RESUME_PATH;
            }
        }
        return SWITCH_USER_RESUME_PATH;
    }

    private String resolveOAuthAuthorizationStartAbsoluteUrl(HttpServletRequest request) {
        String backendBase = SwitchUserIntentSupport.resolveBackendPublicBaseUrl(
                backendPublicBaseUrl, devServerEnabled, frontendDevServerUrl);
        if (StringUtils.hasText(backendBase)) {
            return OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase(backendBase);
        }
        String spaBase = resolveSpaPublicBaseUrl(request);
        if (StringUtils.hasText(spaBase)) {
            return OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase(spaBase);
        }
        return "/oauth2/authorization/authentik?"
                + SwitchUserOAuth2AuthorizationRequestResolver.SWITCH_USER_PARAM + "=1";
    }

    private String resolveAuthentikEndSessionUrl() {
        if (clientRegistrationRepository == null) {
            return null;
        }
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");
        return AuthentikEndSessionUriResolver.resolve(authentikEndSessionUri, registration);
    }

    private String resolveSpaPublicBaseUrl(HttpServletRequest request) {
        return PublicFrontendUrlResolver.resolve(
                switchUserReturnBaseUrl, devServerEnabled, frontendDevServerUrl, request);
    }
}
