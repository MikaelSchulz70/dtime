package se.dtime.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Resolves the browser-facing origin for the SPA (e.g. {@code https://localhost:3000} behind webpack proxy).
 */
public final class PublicFrontendUrlResolver {

    private PublicFrontendUrlResolver() {
    }

    public static String resolve(String switchUserReturnBaseUrl,
                                 boolean devServerEnabled,
                                 String frontendDevServerUrl,
                                 HttpServletRequest request) {
        if (StringUtils.hasText(switchUserReturnBaseUrl)) {
            return trimTrailingSlashes(switchUserReturnBaseUrl.trim());
        }
        if (devServerEnabled) {
            String base = StringUtils.hasText(frontendDevServerUrl)
                    ? frontendDevServerUrl
                    : "https://localhost:3000";
            return trimTrailingSlashes(base.trim());
        }
        if (request != null) {
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (StringUtils.hasText(forwardedHost)) {
                String proto = request.getHeader("X-Forwarded-Proto");
                String scheme = StringUtils.hasText(proto) ? proto : "https";
                return scheme + "://" + forwardedHost.trim();
            }
        }
        return null;
    }

    public static String resolve(String switchUserReturnBaseUrl,
                                 boolean devServerEnabled,
                                 String frontendDevServerUrl) {
        return resolve(switchUserReturnBaseUrl, devServerEnabled, frontendDevServerUrl, null);
    }

    static String trimTrailingSlashes(String url) {
        String trimmed = url;
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
