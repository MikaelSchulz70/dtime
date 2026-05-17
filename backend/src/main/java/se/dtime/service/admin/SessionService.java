package se.dtime.service.admin;

import org.springframework.security.core.Authentication;
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

@Service
public class SessionService {

  private final CalendarService calendarService;
  private final UserRepository userRepository;

  public SessionService(CalendarService calendarService, UserRepository userRepository) {
    this.calendarService = calendarService;
    this.userRepository = userRepository;
  }

  public SessionInfo getSessionInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new NotFoundException("user.not.logged.in");
    }

    LoggedInUser loggedInUser = resolveLoggedInUser(authentication);
    Day currentDay = calendarService.getCurrentDay();

    return SessionInfo.builder().currentDate(currentDay).loggedInUser(loggedInUser).build();
  }

  private LoggedInUser resolveLoggedInUser(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    boolean isAdmin =
        authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

    if (principal instanceof UserExt userExt) {
      return LoggedInUser.builder()
          .userId(userExt.getId())
          .name(formatDisplayName(userExt.getFirstName(), userExt.getLastName()))
          .isAdmin(isAdmin)
          .build();
    }

    if (principal instanceof OAuth2User oauth2User) {
      UserPO user = resolveUserFromOAuth(oauth2User);
      return LoggedInUser.builder()
          .userId(user.getId())
          .name(formatDisplayName(user.getFirstName(), user.getLastName()))
          .isAdmin(isAdmin)
          .build();
    }

    return resolveLoggedInUserByAuthenticationName(authentication.getName(), isAdmin);
  }

  private LoggedInUser resolveLoggedInUserByAuthenticationName(String authenticationName, boolean isAdmin) {
    if (authenticationName == null || authenticationName.isBlank()) {
      throw new NotFoundException("user.not.logged.in");
    }

    UserPO user = userRepository.findByEmail(authenticationName);
    if (user == null) {
      throw new NotFoundException("user.not.logged.in");
    }

    return LoggedInUser.builder()
        .userId(user.getId())
        .name(formatDisplayName(user.getFirstName(), user.getLastName()))
        .isAdmin(isAdmin)
        .build();
  }

  private UserPO resolveUserFromOAuth(OAuth2User oauth2User) {
    String sub = oauth2User.getAttribute("sub");
    if (sub == null || sub.isBlank()) {
      throw new NotFoundException("user.not.logged.in");
    }

    UserPO user = userRepository.findByExternalId(sub);
    if (user == null) {
      throw new NotFoundException("user.not.logged.in");
    }

    return user;
  }

  private static String formatDisplayName(String firstName, String lastName) {
    return (firstName + " " + lastName).trim();
  }
}
