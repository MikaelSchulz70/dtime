package se.dtime.restcontroller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void switchOidcUser_redirectsToPromptLogin_whenOidcEnabled() throws Exception {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        controller.switchOidcUser(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/oauth2/authorization/authentik?prompt=login&max_age=0");
    }

    @Test
    void switchOidcUser_redirectsToLogin_whenOidcDisabled() throws Exception {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authentikOAuthEnabled", false);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);

        controller.switchOidcUser(request, response);

        verify(response).sendRedirect("/oauth2/authorization/authentik");
        verifyNoMoreInteractions(response);
    }
}
