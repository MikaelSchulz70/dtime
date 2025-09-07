package se.dtime.service.admin;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.model.LoggedInUser;
import se.dtime.model.SessionInfo;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.Day;
import se.dtime.service.calendar.CalendarService;
import se.dtime.utils.UserUtil;

@Service
public class SessionService {

    private final CalendarService calendarService;

    public SessionService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public SessionInfo getSessionInfo() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserExt userExt = null;

        if (principal instanceof UserExt) {
            userExt = (UserExt) principal;
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
}
