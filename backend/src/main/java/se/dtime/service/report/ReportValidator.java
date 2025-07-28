package se.dtime.service.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.UserExt;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.CloseDate;
import se.dtime.repository.UserRepository;
import se.dtime.utils.UserUtil;

@Service
public class ReportValidator {
    @Autowired
    private UserRepository userRepository;

    public void validateCloseTimeReport(CloseDate closeDate) {
        userRepository.findById(closeDate.getUserId()).orElseThrow(() -> new ValidationException("user.not.found"));
    }

    public void validateOpenTimeReport(CloseDate closeDate) {
        UserPO userPO = userRepository.findById(closeDate.getUserId()).orElseThrow(() -> new ValidationException("user.not.found"));

        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        if (!isAdmin) {
            throw new ValidationException("time.report.only.admin.can.open");
        }
    }
}
