package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${oauth.google.enabled:false}")
    private boolean googleOAuthEnabled;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/google/status")
    public Map<String, Object> getGoogleAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // OAuth is considered available if it's enabled AND client registration is available
        boolean isAvailable = googleOAuthEnabled && clientRegistrationRepository != null;
        
        response.put("enabled", isAvailable);
        return response;
    }
}