package se.dtime.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Forwards {@code prompt=login} and {@code max_age=0} to the IdP only when the client hit
 * {@code /oauth2/authorization/authentik?switch_user=1} or carries the one-shot session flag set by
 * {@code AuthController#switchOidcUser} (after optional Authentik end-session redirect).
 * Query parameters on the authorization request are not reliably propagated by the default resolver.
 */
public class SwitchUserOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String SWITCH_USER_PARAM = "switch_user";

    /**
     * One-shot flag set in {@code AuthController#switchOidcUser} so re-auth still works if
     * {@code switch_user=1} is dropped by a proxy; removed when the authorization request is built.
     */
    public static final String SESSION_SWITCH_USER_REAUTH = "OAUTH2_SWITCH_USER_REAUTH";

    private final OAuth2AuthorizationRequestResolver delegate;

    public SwitchUserOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return addReauthParams(delegate.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return addReauthParams(delegate.resolve(request, clientRegistrationId), request);
    }

    private OAuth2AuthorizationRequest addReauthParams(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {
        if (authorizationRequest == null) {
            return null;
        }
        if (!isSwitchUserReauth(request)) {
            return authorizationRequest;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_SWITCH_USER_REAUTH);
        }
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(params -> {
                    params.put("prompt", "login");
                    params.put("max_age", "0");
                })
                .build();
    }

    static boolean isSwitchUserReauth(HttpServletRequest request) {
        if ("1".equals(request.getParameter(SWITCH_USER_PARAM))) {
            return true;
        }
        HttpSession session = request.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_SWITCH_USER_REAUTH));
    }
}
