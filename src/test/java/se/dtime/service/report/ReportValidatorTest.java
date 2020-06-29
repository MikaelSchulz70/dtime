package se.dtime.service.report;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserExt;
import se.dtime.model.UserRole;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.CloseDate;
import se.dtime.repository.UserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportValidatorTest {
    @InjectMocks
    private ReportValidator reportValidator;
    @Mock
    private UserRepository userRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateCloseTimeReportOk() {
        UserPO userPO = new UserPO();
        userPO.setUserRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userPO));

        CloseDate closeDate = new CloseDate();
        closeDate.setIdUser(1L);
        reportValidator.validateCloseTimeReport(closeDate);
    }

    @Test
    public void validateCloseTimeReportUserNotFound() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.not.found");

        CloseDate closeDate = new CloseDate();
        closeDate.setIdUser(1L);
        reportValidator.validateCloseTimeReport(closeDate);
    }

    @Test
    public void validateOpenTimeReportOk() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserExt userExt = new UserExt("name", "pwd", authorities, 1, "", "");
        SecurityContextHolder.setContext(createSecurityContext(userExt));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserPO()));

        CloseDate closeDate = new CloseDate();
        closeDate.setIdUser(1L);
        reportValidator.validateOpenTimeReport(closeDate);
    }

    @Test
    public void validateOpenTimeReportUserNotFound() {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.not.found");

        CloseDate closeDate = new CloseDate();
        closeDate.setIdUser(1L);
        reportValidator.validateOpenTimeReport(closeDate);
    }

    @Test
    public void validateOpenTimeReportUserNotAdmin() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserExt userExt = new UserExt("name", "pwd", authorities, 1, "", "");
        SecurityContextHolder.setContext(createSecurityContext(userExt));

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserPO()));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("time.report.only.admin.can.open");

        CloseDate closeDate = new CloseDate();
        closeDate.setIdUser(1L);
        reportValidator.validateOpenTimeReport(closeDate);
    }

    private SecurityContext createSecurityContext(UserExt userExt) {
        return new SecurityContext() {
            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return userExt;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return false;
                    }

                    @Override
                    public void setAuthenticated(boolean b) throws IllegalArgumentException {

                    }

                    @Override
                    public String getName() {
                        return null;
                    }
                };
            }

            @Override
            public void setAuthentication(Authentication authentication) {

            }
        };
    }
}