package se.dtime.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend.dev-server.url:}")
    private String frontendDevServerUrl;

    @Value("${app.frontend.dev-server.enabled:false}")
    private boolean devServerEnabled;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.debug("CustomAuthenticationSuccessHandler called - devServerEnabled: {}, frontendDevServerUrl: {}",
                devServerEnabled, frontendDevServerUrl);

        // Check if request comes from development server
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        String accept = request.getHeader("Accept");

        log.debug("Request headers - Referer: {}, Origin: {}, Accept: {}", referer, origin, accept);

        // In local dev, always return user to frontend dev server after OIDC callback.
        if (devServerEnabled) {
            log.info("Development mode enabled, redirecting to frontend dev server");

            // For AJAX requests, return JSON success response
            if (accept != null && accept.contains("application/json")) {
                log.debug("Returning JSON response for AJAX request");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"redirectUrl\": \"" +
                        (frontendDevServerUrl.isEmpty() ? "https://localhost:3000" : frontendDevServerUrl) + "\"}");
                return;
            }

            // For form submissions, redirect back to dev server with login success parameter
            String baseUrl = frontendDevServerUrl.isEmpty() ? "https://localhost:3000" : frontendDevServerUrl;
            String redirectUrl = baseUrl + "?loginSuccess=true";
            String displayName = resolveDisplayName(authentication);
            if (!displayName.isBlank()) {
                redirectUrl += "&user=" + URLEncoder.encode(displayName, StandardCharsets.UTF_8);
            }
            log.info("Redirecting to dev server: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }

        // Default behavior for production - redirect to backend index
        log.debug("Using default redirect to /index");
        response.sendRedirect("/index");
    }

    private String resolveDisplayName(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Object fullNameClaim = oauth2User.getAttribute("name");
            if (fullNameClaim != null && !fullNameClaim.toString().isBlank()) {
                return fullNameClaim.toString().trim();
            }

            Object email = oauth2User.getAttribute("email");
            if (email != null && !email.toString().isBlank()) {
                return email.toString();
            }
            if (oauth2User.getName() != null) {
                return oauth2User.getName();
            }
        }
        return authentication.getName() == null ? "" : authentication.getName();
    }
}