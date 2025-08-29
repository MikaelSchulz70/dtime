package se.dtime.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Value("${app.frontend.dev-server.url:}")
    private String frontendDevServerUrl;

    @Value("${app.frontend.dev-server.enabled:false}")
    private boolean devServerEnabled;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        logger.debug("CustomAuthenticationSuccessHandler called - devServerEnabled: {}, frontendDevServerUrl: {}", 
                    devServerEnabled, frontendDevServerUrl);
        
        // Check if request comes from development server
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        String accept = request.getHeader("Accept");
        
        logger.debug("Request headers - Referer: {}, Origin: {}, Accept: {}", referer, origin, accept);
        
        // If development server is enabled and request comes from dev server
        if (devServerEnabled && (isFromDevServer(referer) || isFromDevServer(origin))) {
            logger.info("Detected dev server request, redirecting to frontend");
            
            // For AJAX requests, return JSON success response
            if (accept != null && accept.contains("application/json")) {
                logger.debug("Returning JSON response for AJAX request");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"redirectUrl\": \"" + 
                    (frontendDevServerUrl.isEmpty() ? "http://localhost:9000" : frontendDevServerUrl) + "\"}");
                return;
            }
            
            // For form submissions, redirect back to dev server with login success parameter
            String baseUrl = frontendDevServerUrl.isEmpty() ? "http://localhost:9000" : frontendDevServerUrl;
            String redirectUrl = baseUrl + "?loginSuccess=true";
            logger.info("Redirecting to dev server: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }
        
        // Default behavior for production - redirect to backend index
        logger.debug("Using default redirect to /index");
        response.sendRedirect("/index");
    }
    
    private boolean isFromDevServer(String url) {
        return url != null && (url.contains("localhost:9000") || url.contains("127.0.0.1:9000"));
    }
}