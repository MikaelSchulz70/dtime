package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${oauth.authentik.enabled:false}")
    private boolean authentikOAuthEnabled;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

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
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        String loginPath = "/oauth2/authorization/authentik";
        if (!authentikOAuthEnabled) {
            response.sendRedirect(loginPath);
            return;
        }
        String forcePromptLoginPath = UriComponentsBuilder
                .fromPath(loginPath)
                .queryParam("prompt", "login")
                .queryParam("max_age", "0")
                .build()
                .toUriString();
        response.sendRedirect(forcePromptLoginPath);
    }
}