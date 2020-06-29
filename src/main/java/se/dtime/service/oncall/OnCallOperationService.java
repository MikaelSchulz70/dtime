package se.dtime.service.oncall;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.oncall.OnCallAlarmPO;
import se.dtime.dbmodel.oncall.OnCallSessionPO;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.oncall.OnCallAlarm;
import se.dtime.model.oncall.OnCallAlarmContainer;
import se.dtime.model.oncall.OnCallSession;
import se.dtime.repository.OnCallAlarmRepository;
import se.dtime.repository.OnCallSessionRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.oncall.dispatcher.OnCallDispatcher;
import se.dtime.utils.UserUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OnCallOperationService {
    private final static long SESSION_ID = 1;

    @Autowired
    private OnCallSessionRepository onCallSessionRepository;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private OnCallConverter onCallConverter;
    @Autowired
    private OnCallAlarmRepository onCallAlarmRepository;
    @Autowired
    private OnCallDispatcher onCallDispatcher;

    public OnCallSession getOnCallSession() {
        return onCallConverter.toModel(getOnCallSessionPO());
    }

    public OnCallSessionPO getOnCallSessionPO() {
        return onCallSessionRepository.findById(SESSION_ID).orElse(new OnCallSessionPO(SESSION_ID, calendarService.getNowDateTime()));
    }

    public void dispatchOnCallEmails() {
        onCallDispatcher.dispatch();
    }

    public OnCallAlarmContainer getOnCallAlarms() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        ;

        List<OnCallAlarmPO> onCallAlarms;
        if (isAdmin) {
            onCallAlarms = onCallAlarmRepository.findAllByOrderByCreateDateTimeDesc();
        } else {
            onCallAlarms = onCallAlarmRepository.findByUserOrderByCreateDateTimeDesc(new UserPO(userExt.getId()));
        }

        return OnCallAlarmContainer.
                builder().
                onCallAlarms(onCallConverter.toPOs(onCallAlarms)).
                isAdmin(isAdmin).
                build();
    }

    public void updateOnCallAlarm(OnCallAlarm onCallAlarm) {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        ;

        OnCallAlarmPO onCallAlarmPO;
        if (isAdmin) {
            onCallAlarmPO = onCallAlarmRepository.findById(onCallAlarm.getId()).orElseThrow(() -> new NotFoundException("oncall.alarm.not.found"));
        } else {
            onCallAlarmPO = onCallAlarmRepository.findByIdAndUser(onCallAlarm.getId(), new UserPO(userExt.getId()));
            if (onCallAlarmPO == null) {
                throw new NotFoundException("oncall.alarm.not.found");
            }
        }

        onCallAlarmPO.setStatus(onCallAlarm.getStatus());
        onCallAlarmPO.setUpdatedBy(userExt.getId());
        onCallAlarmPO.setUpdatedDateTime(calendarService.getNowDateTime());

        onCallAlarmRepository.save(onCallAlarmPO);
    }

    public void truncateAlarms() {
        LocalDateTime now = calendarService.getNowDateTime();
        onCallAlarmRepository.deleteByCreateDateTimeBefore(now.minusMonths(3));
    }

    @Transactional
    public void deleteAlarm(long idAlarm) {
        onCallAlarmRepository.deleteById(idAlarm);
    }

    public OnCallAlarm getOnCallAlarm(long idAlarm) {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isAdmin = UserUtil.isUserAdmin(userExt);
        ;


        OnCallAlarmPO onCallAlarmPO = onCallAlarmRepository.findById(idAlarm).
                orElseThrow(() -> new NotFoundException("oncall.alarm.not.found"));

        if (!isAdmin && userExt.getId() != onCallAlarmPO.getUser().getId()) {
            throw new NotFoundException("oncall.alarm.not.found");
        }

        return onCallConverter.toPO(onCallAlarmPO);
    }
}
