package se.dtime.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

        // If development server is enabled and request comes from dev server
        if (devServerEnabled && (isFromDevServer(referer) || isFromDevServer(origin))) {
            log.info("Detected dev server request, redirecting to frontend");

            // For AJAX requests, return JSON success response
            if (accept != null && accept.contains("application/json")) {
                log.debug("Returning JSON response for AJAX request");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"redirectUrl\": \"" +
                        (frontendDevServerUrl.isEmpty() ? "https://localhost:9000" : frontendDevServerUrl) + "\"}");
                return;
            }

            // For form submissions, redirect back to dev server with login success parameter
            String baseUrl = frontendDevServerUrl.isEmpty() ? "https://localhost:9000" : frontendDevServerUrl;
            String redirectUrl = baseUrl + "?loginSuccess=true";
            log.info("Redirecting to dev server: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }

        // Default behavior for production - redirect to backend index
        log.debug("Using default redirect to /index");
        response.sendRedirect("/index");
    }

    private boolean isFromDevServer(String url) {
        return url != null && (url.contains("localhost:9000") || url.contains("127.0.0.1:9000"));
    }
}