package se.dtime.service.timeentry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.dbmodel.timereport.TimeEntryPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.ReportDates;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.timereport.*;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.user.UserValidator;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TimeEntryService {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TimeEntryRepository timeEntiryRepository;
    @Autowired
    private TimeReportConverter timeReportConverter;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private TimeReportValidator timeReportValidator;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;

    public long addOrUpdate(TimeEntry timeEntry) {
        log.debug("Time: {} {}", timeEntry.getTime(), timeEntry.getDay().getDate());
        userValidator.validateLoggedIn();

        // User is just tabbing around in the grid
        boolean noTime = timeEntry.getTime() == null || timeEntry.getTime() == 0;
        if (noTime) {
            boolean exists = timeEntiryRepository.existsById(timeEntry.getId());
            if (exists) {
                timeEntiryRepository.deleteById(timeEntry.getId());
            }
            return 0L;
        }

        timeReportValidator.validateAdd(timeEntry);

        TimeEntryPO timeReportDayPO = timeEntiryRepository.findByTaskContributorAndDate(timeEntry.getTaskContributorId(), timeEntry.getDay().getDate());
        if (timeReportDayPO != null) {
            timeReportDayPO.setTime(timeEntry.getTime());
            timeReportConverter.updateBaseDate(timeReportDayPO);
        } else {
            timeReportDayPO = timeReportConverter.toPO(timeEntry);
        }

        timeEntiryRepository.save(timeReportDayPO);

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

    public TimeReport getUserTimeReport(long userId, TimeReportView timeReportView, LocalDate date) {
        ReportDates reportDates = getReportDates(timeReportView, date);
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));
        return getTimeReportBetweenDatesForUser(reportDates, userPO, timeReportView);
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
        List<TimeEntryPO> timeReportDayPOS = timeEntiryRepository.findByUserAndBetweenDates(userPO.getId(), reportDates.getFromDate(), reportDates.getToDate());
        List<TaskContributorPO> assignments = taskContributorRepository.findByUserAndActivationStatus(userPO, ActivationStatus.ACTIVE);

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
        return switch (timeReportView) {
            case WEEK -> date.minusWeeks(1);
            case MONTH -> date.minusMonths(1);
        };
    }

    LocalDate getNextDate(TimeReportView timeReportView, LocalDate date) {
        return switch (timeReportView) {
            case WEEK -> date.plusWeeks(1);
            case MONTH -> date.plusMonths(1);
        };
    }

    ReportDates getReportDates(TimeReportView timeReportView, LocalDate date) {
        LocalDate fromDate;
        LocalDate toDate = switch (timeReportView) {
            case WEEK -> {
                fromDate = calendarService.getClosestMonday(date);
                yield fromDate.plusDays(6);
            }
            case MONTH -> {
                fromDate = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
                yield LocalDate.of(date.getYear(), date.getMonthValue(), fromDate.lengthOfMonth());
            }
        };

        return new ReportDates(fromDate, toDate);
    }

    void updateClosedDays(TimeReport timeReport, Set<LocalDate> closedMonths) {
        updateClosedDays(timeReport.getTimeReportTasks(), closedMonths);
    }

    private void updateClosedDays(List<TimeReportTask> timeReportTask, Set<LocalDate> closedMonths) {
        timeReportTask.
                stream().
                flatMap(t -> t.getTimeEntries().stream()).
                forEach(t -> updateClosedDay(t, closedMonths));
    }

    private void updateClosedDay(TimeEntry timeEntry, Set<LocalDate> closedMonths) {
        LocalDate systemStartDate = calendarService.getSystemStartDate();
        if (timeEntry.getDay().getDate().isBefore(systemStartDate)) {
            timeEntry.setClosed(true);
            return;
        }

        for (LocalDate closeDate : closedMonths) {
            if (closeDate.getYear() == timeEntry.getDay().getDate().getYear() &&
                    closeDate.getMonthValue() == timeEntry.getDay().getDate().getMonthValue()) {
                timeEntry.setClosed(true);
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
}
