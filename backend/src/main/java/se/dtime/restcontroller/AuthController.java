package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import se.dtime.config.AuthentikEndSessionUriResolver;
import se.dtime.config.OidcSwitchUserRedirectSupport;
import se.dtime.config.SwitchUserOAuth2AuthorizationRequestResolver;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${oauth.authentik.enabled:false}")
    private boolean authentikOAuthEnabled;

    @Value("${oauth.authentik.end-session-uri:}")
    private String authentikEndSessionUri;

    @Value("${oauth.authentik.full-logout-flow-uri:}")
    private String authentikFullLogoutFlowUri;

    /**
     * Optional override for {@code post_logout_redirect_uri} during switch-user (must match Authentik allowlist exactly).
     */
    @Value("${oauth.authentik.switch-user-return-base-url:}")
    private String switchUserReturnBaseUrl;

    @Value("${app.frontend.dev-server.url:}")
    private String frontendDevServerUrl;

    @Value("${app.frontend.dev-server.enabled:false}")
    private boolean devServerEnabled;

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

    @GetMapping("/oidc/switch-user")
    public void switchOidcUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (rememberMeServices != null) {
            rememberMeServices.logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
        }
        SecurityContextHolder.clearContext();

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        String loginPath = "/oauth2/authorization/authentik";
        if (!authentikOAuthEnabled) {
            response.sendRedirect(loginPath);
            return;
        }

        HttpSession freshSession = request.getSession(true);
        freshSession.setAttribute(SwitchUserOAuth2AuthorizationRequestResolver.SESSION_SWITCH_USER_REAUTH,
                Boolean.TRUE);

        String endSessionEndpoint = resolveAuthentikEndSessionUrl();
        String fullLogoutFlowEndpoint = resolveAuthentikFullLogoutFlowUrl();
        if ((StringUtils.hasText(fullLogoutFlowEndpoint) || StringUtils.hasText(endSessionEndpoint))
                && clientRegistrationRepository != null) {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");
            if (registration != null) {
                String postLogoutRedirect = resolveOAuthRestartAbsoluteUrl(request);
                String logoutTarget = StringUtils.hasText(fullLogoutFlowEndpoint)
                        ? fullLogoutFlowEndpoint
                        : endSessionEndpoint;
                String idpLogout = UriComponentsBuilder.fromUriString(logoutTarget)
                        .queryParam("client_id", registration.getClientId())
                        .queryParam("post_logout_redirect_uri", postLogoutRedirect)
                        .queryParam("next", postLogoutRedirect)
                        .encode()
                        .build()
                        .toUriString();
                response.sendRedirect(idpLogout);
                return;
            }
        }

        response.sendRedirect(loginPath);
    }

    private String resolveAuthentikEndSessionUrl() {
        if (clientRegistrationRepository == null) {
            return null;
        }
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");
        return AuthentikEndSessionUriResolver.resolve(authentikEndSessionUri, registration);
    }

    private String resolveAuthentikFullLogoutFlowUrl() {
        if (clientRegistrationRepository == null) {
            return null;
        }
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");
        return AuthentikEndSessionUriResolver.resolveFullInvalidationFlowUri(
                authentikFullLogoutFlowUri,
                authentikEndSessionUri,
                registration
        );
    }

    /**
     * After IdP logout, the browser must land on an OAuth2 start URL that Authentik allowlists.
     * In local dev the user uses {@code https://localhost:3000} (webpack proxy) while the API may be
     * served as {@code localhost:8443}; use the same base as {@link se.dtime.config.CustomAuthenticationSuccessHandler}.
     */
    private String resolveOAuthRestartAbsoluteUrl(HttpServletRequest request) {
        if (StringUtils.hasText(switchUserReturnBaseUrl)) {
            return OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase(switchUserReturnBaseUrl.trim());
        }
        if (devServerEnabled) {
            String base = StringUtils.hasText(frontendDevServerUrl) ? frontendDevServerUrl : "https://localhost:3000";
            return OidcSwitchUserRedirectSupport.buildOAuthRestartUrlFromPublicBase(base);
        }
        return OidcSwitchUserRedirectSupport.buildOAuthRestartUrl(request);
    }
}