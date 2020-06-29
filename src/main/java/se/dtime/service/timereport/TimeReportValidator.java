package se.dtime.service.timereport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.TimeReportDay;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.CloseDateRepository;
import se.dtime.service.calendar.CalendarService;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimeReportValidator extends ValidatorBase<TimeReportDay> {
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;

    public static final int MIN_TIME = 0;
    public static final int MAX_TIME = 24;
    public static final int MAX_NUMBER_OF_DECIMALS = 2;
    public static final int FIRST_WEEK_OF_YEAR = 1;
    public static final int LAST_WEEK_OF_YEAR = 52;
    public static final int FIRST_MONTH_OF_YEAR = 1;
    public static final int LAST_MONTH_OF_YEAR = 12;
    public static final int FIRST_YEAR = 0;
    public static final int FIRST_DAY_OF_MOUTH = 1;
    static final String FIELD_DATE = "time";

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
            VALIDATOR_MAP.put(FIELD_DATE, new TimeReportDayValidator());
        }
    }

    public void validateAdd(TimeReportDay timeReportDay) {
        AssignmentPO assignmentPO = assignmentRepository.findById(timeReportDay.getIdAssignment()).orElseThrow(() -> new ValidationException("time.report.assignment.not.found"));
        check(assignmentPO.getActivationStatus() == ActivationStatus.ACTIVE, "time.report.assignment.not.active");

        if (timeReportDay.getTime() != null) {
            checkInvalidInput(timeReportDay.getTime() >= MIN_TIME, "time.report.invalid.time.min");
            checkInvalidInput(timeReportDay.getTime() <= MAX_TIME, "time.report.invalid.time.max");

            String timeStr = Float.toString(timeReportDay.getTime());
            int integerPlaces = timeStr.indexOf('.');
            if (integerPlaces != -1) {
                int decimalPlaces = timeStr.length() - integerPlaces - 1;
                checkInvalidInput(decimalPlaces <= MAX_NUMBER_OF_DECIMALS, "time.report.invalid.time.decimals");
            }
        }

        validateClosed(timeReportDay, assignmentPO.getUser());
    }

    private void validateClosed(TimeReportDay timeReportDay, UserPO userPO) {
        LocalDate date = LocalDate.of(timeReportDay.getDay().getYear(), timeReportDay.getDay().getMonth(), 1);
        CloseDatePO closeDatePO = closeDateRepository.findByUserAndDate(userPO, date);
        check (closeDatePO == null, "time.report.is.closed");
    }

    public void validateDelete(long id) {

    }

    public void validateUpdate(TimeReportDay timeReportDay) {

    }

    void validateYearWeek(int year, int week) {
        checkInvalidInput(year >= FIRST_YEAR, "invalid.year.input");
        checkInvalidInput(week >= FIRST_WEEK_OF_YEAR && week <= LAST_WEEK_OF_YEAR, "invalid.week.input");
    }

    void validateYearMouth(int year, int month) {
        checkInvalidInput(year >= FIRST_YEAR, "invalid.year.input");
        checkInvalidInput(month >= FIRST_MONTH_OF_YEAR && month <= LAST_MONTH_OF_YEAR, "invalid.month.input");
    }

    void validateYearMouthDay(int year, int month, int day) {
        checkInvalidInput(year >= FIRST_YEAR, "invalid.year.input");
        checkInvalidInput(month >= FIRST_MONTH_OF_YEAR && month <= LAST_MONTH_OF_YEAR, "invalid.month.input");

        int numberOfDaysInMouth = calendarService.getNumberOfDaysInMonth(year, month);
        checkInvalidInput(day >= FIRST_DAY_OF_MOUTH && month <= numberOfDaysInMouth, "invalid.day.input");
    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

    class TimeReportDayValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            float time = 0;
            try {
                time = Float.valueOf(attribute.getValue());
            } catch (NumberFormatException e) {
                throw new ValidationException(attribute.getName(), "time.not.a.number");
            }

            check(time > 0 && time <= 24, attribute.getName(),"time.not.within.valid.span");;
        }
    }
}
