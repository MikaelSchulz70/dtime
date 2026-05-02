package se.dtime.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Builds absolute URLs for OIDC post-logout redirect back into this application.
 */
public final class OidcSwitchUserRedirectSupport {

    private OidcSwitchUserRedirectSupport() {
    }

    /**
     * Absolute URL to restart OAuth2 login with {@code switch_user=1} so the authorization resolver
     * adds {@code prompt=login} even if the one-shot session attribute was lost across domains.
     */
    public static String buildOAuthRestartUrl(HttpServletRequest request) {
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName());
        int port = request.getServerPort();
        if (needsExplicitPort(request.isSecure(), port)) {
            builder.port(port);
        }
        builder.path(contextPath + "/oauth2/authorization/authentik");
        builder.queryParam(SwitchUserOAuth2AuthorizationRequestResolver.SWITCH_USER_PARAM, "1");
        return builder.build().encode().toUriString();
    }

    /**
     * Same as {@link #buildOAuthRestartUrl(HttpServletRequest)} but from a fixed public base
     * (for example {@code https://localhost:3000} behind the webpack dev proxy).
     */
    public static String buildOAuthRestartUrlFromPublicBase(String publicBaseUrl) {
        if (!StringUtils.hasText(publicBaseUrl)) {
            throw new IllegalArgumentException("publicBaseUrl must not be blank");
        }
        String trimmed = publicBaseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        URI oauthStart = URI.create(trimmed).resolve("/oauth2/authorization/authentik");
        return UriComponentsBuilder.fromUri(oauthStart)
                .queryParam(SwitchUserOAuth2AuthorizationRequestResolver.SWITCH_USER_PARAM, "1")
                .encode()
                .build()
                .toUriString();
    }

    private static boolean needsExplicitPort(boolean secure, int port) {
        if (port <= 0) {
            return false;
        }
        return !((secure && port == 443) || (!secure && port == 80));
    }
}
