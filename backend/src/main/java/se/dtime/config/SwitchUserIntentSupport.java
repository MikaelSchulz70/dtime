package se.dtime.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

/**
 * Persists switch-user intent across the Authentik end-session round-trip (session may be lost).
 */
public final class SwitchUserIntentSupport {

    public static final String COOKIE_NAME = "DTIME_SWITCH_USER";

    /** Set when switch-user starts (before optional IdP end-session). */
    public static final String COOKIE_VALUE_SWITCH = "1";

    /**
     * Set on {@code /switch-user/continue} after Authentik end-session. OAuth should not use
     * {@code prompt=login} again or the user is often asked to sign in twice at the IdP.
     */
    public static final String COOKIE_VALUE_POST_LOGOUT = "post_logout";

    private SwitchUserIntentSupport() {
    }

    public static void markSwitchUserIntent(HttpServletRequest request, HttpServletResponse response) {
        addIntentCookie(request, response, COOKIE_VALUE_SWITCH);
    }

    public static void markSwitchUserIntentAfterIdpLogout(HttpServletRequest request, HttpServletResponse response) {
        addIntentCookie(request, response, COOKIE_VALUE_POST_LOGOUT);
    }

    public static void clearSwitchUserIntent(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public static boolean hasSwitchUserIntent(HttpServletRequest request) {
        return cookieValue(request) != null;
    }

    public static boolean isAfterIdpLogout(HttpServletRequest request) {
        return COOKIE_VALUE_POST_LOGOUT.equals(cookieValue(request));
    }

    public static String resolveBackendPublicBaseUrl(String configuredBackendPublicBaseUrl,
                                                     boolean devServerEnabled,
                                                     String frontendDevServerUrl) {
        if (StringUtils.hasText(configuredBackendPublicBaseUrl)) {
            return PublicFrontendUrlResolver.trimTrailingSlashes(configuredBackendPublicBaseUrl.trim());
        }
        if (devServerEnabled) {
            return "https://localhost:8443";
        }
        return null;
    }

    private static void addIntentCookie(HttpServletRequest request, HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(request != null && request.isSecure());
        cookie.setMaxAge(600);
        response.addCookie(cookie);
    }

    private static String cookieValue(HttpServletRequest request) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String value = cookie.getValue();
                if (COOKIE_VALUE_SWITCH.equals(value) || COOKIE_VALUE_POST_LOGOUT.equals(value)) {
                    return value;
                }
            }
        }
        return null;
    }
}
