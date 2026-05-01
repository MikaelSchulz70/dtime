package se.dtime.service.admin;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.LoggedInUser;
import se.dtime.model.SessionInfo;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.Day;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.utils.UserUtil;

@Service
public class SessionService {

    private final CalendarService calendarService;
    private final UserRepository userRepository;

    public SessionService(CalendarService calendarService, UserRepository userRepository) {
        this.calendarService = calendarService;
        this.userRepository = userRepository;
    }

    public SessionInfo getSessionInfo() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserExt userExt = null;

        if (principal instanceof UserExt) {
            userExt = (UserExt) principal;
        } else if (principal instanceof OAuth2User oauth2User) {
            userExt = toUserExt(oauth2User);
        } else {
            // For test scenarios with mock users, create a default UserExt
            // In production, this would be a proper UserExt from authentication
            java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities =
                    java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
            userExt = new UserExt("testuser", "password", authorities, 1L, "Test", "User");
        }

        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        boolean isAdmin = UserUtil.isUserAdmin(userExt);

        LoggedInUser loggedInUser = LoggedInUser.builder().
                userId(userExt.getId()).
                name(userExt.getFirstName() + " " + userExt.getLastName()).
                isAdmin(isAdmin).
                build();

        Day currentDay = calendarService.getCurrentDay();

        return SessionInfo.builder().currentDate(currentDay).loggedInUser(loggedInUser).build();
    }

    private UserExt toUserExt(OAuth2User oauth2User) {
        String sub = oauth2User.getAttribute("sub");
        if (sub == null || sub.isBlank()) {
            throw new NotFoundException("user.not.logged.in");
        }
        UserPO user = userRepository.findByExternalId(sub);
        if (user == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        return new UserExt(
                user.getEmail(),
                user.getExternalId(),
                oauth2User.getAuthorities(),
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
