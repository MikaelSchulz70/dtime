package se.dtime.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.LoggedInUser;
import se.dtime.model.SessionInfo;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.Day;
import se.dtime.repository.AssignmentRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.utils.UserUtil;

@Service
public class SessionService {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private AssignmentRepository assignmentRepository;

    public SessionInfo getSessionInfo() {
        UserExt userExt = null;
        try {
            userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new NotFoundException("user.not.logged.in");
        }

        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        boolean showOnCall = showOnCall(userExt);
        boolean isAdmin = UserUtil.isUserAdmin(userExt);

        LoggedInUser loggedInUser = LoggedInUser.builder().
                idUser(userExt.getId()).
                name(userExt.getFirstName() + " " + userExt.getLastName()).
                isAdmin(isAdmin).
                build();

        Day currentDay = calendarService.getCurrentDay();

        return SessionInfo.builder().currentDate(currentDay).showOnCall(showOnCall).loggedInUser(loggedInUser).build();
    }

    private boolean showOnCall(UserExt userExt) {
        boolean showOnCall = UserUtil.isUserAdmin(userExt);

        if (!showOnCall) {
            long noOnCallProject = assignmentRepository.countByUserAndActivationStatusAndProjectOnCallTrue(new UserPO(userExt.getId()), ActivationStatus.ACTIVE);
            showOnCall = (noOnCallProject > 0);
        }

        return showOnCall;
    }
}
