package se.dtime.service.timereport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.dbmodel.timereport.TimeReportDayPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Constants;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.*;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TimeReportRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.user.UserValidator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TimeReportService {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TimeReportRepository timeReportRepository;
    @Autowired
    private TimeReportConverter timeReportConverter;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private TimeReportValidator timeReportValidator;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;

    public long addOrUpdate(TimeReportDay timeReportDay) {
        log.debug("Time: {} {}", timeReportDay.getTime(), timeReportDay.getDay().getDate());
        userValidator.validateLoggedIn();

        // User is just tabbing around in the grid
        boolean noTime = timeReportDay.getTime() == null || timeReportDay.getTime() == 0;
        if (noTime) {
            boolean exists = timeReportRepository.existsById(timeReportDay.getId());
            if (exists) {
                timeReportRepository.deleteById(timeReportDay.getId());
            }
            return 0L;
        }

        timeReportValidator.validateAdd(timeReportDay);

        TimeReportDayPO timeReportDayPO = timeReportRepository.findByAssignmentAndDate(timeReportDay.getIdAssignment(), timeReportDay.getDay().getDate());
        if (timeReportDayPO != null) {
            timeReportDayPO.setTime(timeReportDay.getTime());
            timeReportConverter.updateBaseDate(timeReportDayPO);
        } else {
            timeReportDayPO = timeReportConverter.toPO(timeReportDay);
        }

        timeReportRepository.save(timeReportDayPO);

        return timeReportDayPO.getId();
    }

    public TimeReport getCurrentTimeReport(TimeReportView timeReportView) {
        if (timeReportView == null) {
            timeReportView = TimeReportView.WEEK;
        }

        return getTimeReport(calendarService.getNowDate(), timeReportView);
    }


    public TimeReport getNextTimeReport(TimeReportView timeReportView, LocalDate date) {
        LocalDate reportDate = getNextDate(timeReportView, date);
        return getTimeReport(reportDate, timeReportView);
    }

    public TimeReport getPreviousTimeReport(TimeReportView timeReportView, LocalDate date) {
        LocalDate reportDate = getPreviousDate(timeReportView, date);
        return getTimeReport(reportDate, timeReportView);
    }

    public TimeReport getUserTimeReport(long idUser, TimeReportView timeReportView, LocalDate date) {
        ReportDates reportDates = getReportDates(timeReportView, date);
        UserPO userPO = userRepository.findById(idUser).orElseThrow(() -> new NotFoundException("user.not.found"));
        return getTimeReportBetweenDatesForUser(reportDates, userPO, timeReportView);
    }

    public Vacations getCurrentVacations() {
        return getVacations(calendarService.getNowDate());
    }

    public Vacations getPreviousVacations(LocalDate date) {
        LocalDate previousDate = getPreviousDate(TimeReportView.MONTH, date);
        return getVacations(previousDate);
    }

    public Vacations getNextVacations(LocalDate date) {
        LocalDate nextDate = getNextDate(TimeReportView.MONTH, date);
        return getVacations(nextDate);
    }

    private TimeReport getTimeReport(LocalDate date, TimeReportView timeReportView) {
        ReportDates reportDates = getReportDates(timeReportView, date);
        return getTimeReportBetweenDates(reportDates, timeReportView);
    }

    private TimeReport getTimeReportBetweenDates(ReportDates reportDates, TimeReportView timeReportView) {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userExt == null) {
            throw new NotFoundException("user.not.logged.in");
        }

        UserPO userPO = userRepository.findById(userExt.getId()).orElseThrow(() -> new NotFoundException("user.not.found"));
        return getTimeReportBetweenDatesForUser(reportDates, userPO, timeReportView);
    }

    private TimeReport getTimeReportBetweenDatesForUser(ReportDates reportDates, UserPO userPO, TimeReportView timeReportView) {
        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        List<TimeReportDayPO> timeReportDayPOS = timeReportRepository.findByUserAndBetweenDates(userPO.getId(), reportDates.getFromDate(), reportDates.getToDate());
        List<AssignmentPO> assignments = assignmentRepository.findByUserAndActivationStatus(userPO, ActivationStatus.ACTIVE);

        TimeReport timeReport = timeReportConverter.convertToTimeReport(days, userPO, timeReportDayPOS, assignments);
        timeReport.setWorkableHours(calendarService.calcWorkableHours(days));

        Set<LocalDate> closedMonths = getClosedMonths(days, userPO);
        updateClosedDays(timeReport, closedMonths);

        if (timeReportView == TimeReportView.MONTH && !closedMonths.isEmpty()) {
            timeReport.setClosed(true);
        }

        return timeReport;
    }

    LocalDate getPreviousDate(TimeReportView timeReportView, LocalDate date) {
        LocalDate reportDate = date;
        switch (timeReportView) {
            case DAY:
                reportDate = date.minusDays(1);
                break;
            case WEEK:
                reportDate = date.minusWeeks(1);
                break;
            case MONTH:
                reportDate = date.minusMonths(1);
                break;
        }
        return reportDate;
    }

    LocalDate getNextDate(TimeReportView timeReportView, LocalDate date) {
        LocalDate reportDate = date;
        switch (timeReportView) {
            case DAY:
                reportDate = date.plusDays(1);
                break;
            case WEEK:
                reportDate = date.plusWeeks(1);
                break;
            case MONTH:
                reportDate = date.plusMonths(1);
                break;
        }
        return reportDate;
    }

    ReportDates getReportDates(TimeReportView timeReportView, LocalDate date) {
        LocalDate fromDate = date;
        LocalDate toDate = date;
        switch (timeReportView) {
            case DAY:
                fromDate = date;
                toDate = date;
                break;
            case WEEK:
                fromDate = calendarService.getClosestMonday(date);
                toDate = fromDate.plusDays(6);
                break;
            case MONTH:
                fromDate = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
                toDate = LocalDate.of(date.getYear(), date.getMonthValue(), fromDate.lengthOfMonth());
                break;
        }

        return new ReportDates(fromDate, toDate);
    }

    void updateClosedDays(TimeReport timeReport, Set<LocalDate> closedMonths) {
        updateClosedDays(timeReport.getTimeReportProjectsExternal(), closedMonths);
        updateClosedDays(timeReport.getTimeReportProjectsInternal(), closedMonths);
    }

    private void updateClosedDays(List<TimeReportProject> timeReportProject, Set<LocalDate> closedMonths) {
        timeReportProject.
                stream().
                flatMap(t -> t.getTimeReportDays().stream()).
                forEach(t -> updateClosedDay(t, closedMonths));
    }

    private void updateClosedDay(TimeReportDay timeReportDay, Set<LocalDate> closedMonths) {
        LocalDate systemStartDate = calendarService.getSystemStartDate();
        if (timeReportDay.getDay().getDate().isBefore(systemStartDate)) {
            timeReportDay.setClosed(true);
            return;
        }

        for (LocalDate closeDate : closedMonths) {
            if (closeDate.getYear() == timeReportDay.getDay().getDate().getYear() &&
                    closeDate.getMonthValue() == timeReportDay.getDay().getDate().getMonthValue()) {
                timeReportDay.setClosed(true);
            }
        }
    }

    Set<LocalDate> getClosedMonths(Day[] days, UserPO userPO) {
        Set<LocalDate> yearMonthSet = new HashSet<>();
        for (Day day : days) {
            LocalDate date = LocalDate.of(day.getYear(), day.getMonth(), 1);
            if (!yearMonthSet.contains(date)) {
                yearMonthSet.add(date);
            }
        }

        Set<LocalDate> closedMonths = new HashSet<>();
        for (LocalDate date : yearMonthSet) {
            CloseDatePO closeDatePO = closeDateRepository.findByUserAndDate(userPO, date);
            if (closeDatePO != null) {
                closedMonths.add(closeDatePO.getDate());
            }
        }

        return closedMonths;
    }

    private Vacations getVacations(LocalDate date) {
        ReportDates reportDates = getReportDates(TimeReportView.MONTH, date);
        Day[] days = calendarService.getDays(reportDates.getFromDate(), reportDates.getToDate());
        List<TimeReportDayPO> timeReportDayPOS = timeReportRepository.findByProjectAndBetweenDates(Constants.PROJECT_VACATION, reportDates.getFromDate(), reportDates.getToDate());
        List<UserPO> userPOs = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);

        List<UserVacation> userVacations = timeReportConverter.convertToUserVacations(userPOs, timeReportDayPOS, days);

        return Vacations.builder().days(days).userVacations(userVacations).build();
    }

}
