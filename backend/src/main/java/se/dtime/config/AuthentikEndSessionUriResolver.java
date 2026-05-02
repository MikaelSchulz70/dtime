package se.dtime.config;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves Authentik OIDC end-session (RP-initiated logout) URL so "switch user" can clear the IdP
 * browser session before starting a new authorization request.
 */
public final class AuthentikEndSessionUriResolver {

    private static final Pattern AUTHENTIK_SLUGGED_AUTHORIZE =
            Pattern.compile("(.*/application/o/[^/]+/)authorize/?$", Pattern.CASE_INSENSITIVE);
    private static final String AUTHENTIK_PROVIDER_PATH_SEGMENT = "/application/o/";
    private static final String AUTHENTIK_FULL_INVALIDATION_PATH = "/if/flow/default-invalidation-flow/";

    private AuthentikEndSessionUriResolver() {
    }

    /**
     * @param configuredEndSessionUri optional {@code oauth.authentik.end-session-uri}
     * @param registration            Authentik client registration (may be null)
     */
    public static String resolve(String configuredEndSessionUri, ClientRegistration registration) {
        if (StringUtils.hasText(configuredEndSessionUri)) {
            return configuredEndSessionUri.trim();
        }
        if (registration == null) {
            return null;
        }
        String authorizationUri = registration.getProviderDetails().getAuthorizationUri();
        if (!StringUtils.hasText(authorizationUri)) {
            return null;
        }
        return deriveFromAuthorizationUri(authorizationUri.trim());
    }

    /**
     * Resolves a full logout flow URL that clears the authentik account session without a manual click.
     * This is used for "switch user", where we want to force account re-authentication.
     */
    public static String resolveFullInvalidationFlowUri(String configuredFlowUri,
                                                        String configuredEndSessionUri,
                                                        ClientRegistration registration) {
        if (StringUtils.hasText(configuredFlowUri)) {
            return configuredFlowUri.trim();
        }
        return null;
    }

    /**
     * Authentik provider URLs are typically
     * {@code .../application/o/{provider-slug}/authorize/} with end-session at
     * {@code .../application/o/{provider-slug}/end-session/}.
     */
    static String deriveFromAuthorizationUri(String authorizationUri) {
        Matcher m = AUTHENTIK_SLUGGED_AUTHORIZE.matcher(authorizationUri);
        if (m.matches()) {
            return m.group(1) + "end-session/";
        }
        if (authorizationUri.contains("/authorize/")) {
            return authorizationUri.replace("/authorize/", "/end-session/");
        }
        return null;
    }

    static String deriveFlowFromProviderUri(String providerUri) {
        int providerPathStart = providerUri.indexOf(AUTHENTIK_PROVIDER_PATH_SEGMENT);
        if (providerPathStart < 0) {
            return null;
        }
        String baseUri = providerUri.substring(0, providerPathStart);
        if (baseUri.endsWith("/")) {
            return baseUri.substring(0, baseUri.length() - 1) + AUTHENTIK_FULL_INVALIDATION_PATH;
        }
        return baseUri + AUTHENTIK_FULL_INVALIDATION_PATH;
    }
}
