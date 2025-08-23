package se.dtime.service.timeentry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.InvalidInputException;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.TimeEntry;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TimeReportValidatorTest {
    @InjectMocks
    private TimeReportValidator timeReportValidator;
    @Mock
    private CalendarService calendarService;
    @Mock
    private TaskContributorRepository taskContributorRepository;
    @Mock
    private CloseDateRepository closeDateRepository;

    @BeforeEach
    public void setup() {
        timeReportValidator.init();
    }

    @Test
    public void validateOk() {
        Attribute time = Attribute.builder().id(1).name("time").value("1").build();
        timeReportValidator.validate(time);

        time.setValue("24");
        timeReportValidator.validate(time);

        time.setValue("0.1");
        timeReportValidator.validate(time);
    }

    @Test
    public void validateTimeNotNumber() {
        Attribute time = Attribute.builder().id(1).name("time").value("ABC").build();

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            timeReportValidator.validate(time);
        });
        assert exception.getMessage().contains("time.not.a.number");
    }

    @Test
    public void validateTimeNegative() {
        Attribute time = Attribute.builder().id(1).name("time").value("-1").build();

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            timeReportValidator.validate(time);
        });
        assert exception.getMessage().contains("time.not.within.valid.span");
    }

    @Test
    public void validateTimeToBig() {
        Attribute time = Attribute.builder().id(1).name("time").value("25").build();

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            timeReportValidator.validate(time);
        });
        assert exception.getMessage().contains("time.not.within.valid.span");
    }

    @Test
    public void testValidateYearMouthOk() {
        timeReportValidator.validateYearMouth(2018, 10);
    }

    @Test
    public void testValidateYearMouthInvalidYear() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouth(-1, 10);
        });
    }

    @Test
    public void testValidateYearMouthInvalidMonth() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouth(2018, 14);
        });
    }

    @Test
    public void testValidateYearWeekOk() {
        timeReportValidator.validateYearWeek(2018, 44);
    }

    @Test
    public void testValidateYearWeekInvalidYear() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearWeek(-1, 10);
        });
    }

    @Test
    public void testValidateYearWeekInvalidWeek() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouth(2018, 53);
        });
    }

    @Test
    public void testValidateYearMouthDayOk() {
        when(calendarService.getNumberOfDaysInMonth(2018, 10)).thenReturn(31);
        timeReportValidator.validateYearMouthDay(2018, 10, 8);
    }

    @Test
    public void testValidateYearMouthDayInvalidYear() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouthDay(-1, 10, 8);
        });
    }

    @Test
    public void testValidateYearMouthDayInvalidMonth() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouthDay(2018, 0, 8);
        });
    }

    @Test
    public void testValidateYearMouthDayInvalidDay() {
        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateYearMouthDay(2018, 10, 32);
        });
    }

    @Test
    public void validateTimeReportDayTaskContributorNotFound() {
        TimeEntry timeEntry = TimeEntry.builder().taskContributorId(1).build();

        assertThrows(ValidationException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    @Test
    public void validateTimeReportDayInactiveTaskContributor() {
        TimeEntry timeEntry = TimeEntry.builder().taskContributorId(1).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO(1);
        TaskContributorPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        assertThrows(ValidationException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    @Test
    public void validateTimeReportDayTimeTooSmall() {
        TimeEntry timeEntry = TimeEntry.builder().taskContributorId(1).time(-1f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    @Test
    public void validateTimeReportDayTimeTooLarge() {
        TimeEntry timeEntry = TimeEntry.builder().taskContributorId(1).time(25f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    @Test
    public void validateTimeReportDayTooManyDecimals() {
        TimeEntry timeEntry = TimeEntry.builder().day(createDay()).taskContributorId(1).time(8.112f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        assertThrows(InvalidInputException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    @Test
    public void validateTimeReportDayNoDecimals() {
        TimeEntry timeEntry = TimeEntry.builder().day(createDay()).taskContributorId(1).time(8.f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        timeReportValidator.validateAdd(timeEntry);
    }

    @Test
    public void validateTimeReportDayNoDecimals1() {
        TimeEntry timeEntry = TimeEntry.builder().day(createDay()).taskContributorId(1).time(8f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        timeReportValidator.validateAdd(timeEntry);
    }

    @Test
    public void validateTimeReportDayOneDecimal() {
        TimeEntry timeEntry = TimeEntry.builder().day(createDay()).taskContributorId(1).time(8.5f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        timeReportValidator.validateAdd(timeEntry);
    }

    @Test
    public void validateTimeReportDayTwoDecimals() {
        TimeEntry timeEntry = TimeEntry.builder().day(createDay()).taskContributorId(1).time(8.55f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));

        timeReportValidator.validateAdd(timeEntry);
    }

    @Test
    public void validateTimeReportClosed() {
        Day day = createDay();
        TimeEntry timeEntry = TimeEntry.builder().day(day).taskContributorId(1).time(8.f).build();

        TaskContributorPO TaskContributorPO = new TaskContributorPO();
        UserPO userPO = new UserPO(1L);
        TaskContributorPO.setUser(userPO);
        TaskContributorPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(taskContributorRepository.findById(1L)).thenReturn(Optional.of(TaskContributorPO));
        when(closeDateRepository.findByUserAndDate(userPO, LocalDate.of(day.getYear(), day.getMonth(), 1))).thenReturn(new CloseDatePO());

        assertThrows(ValidationException.class, () -> {
            timeReportValidator.validateAdd(timeEntry);
        });
    }

    private Day createDay() {
        LocalDate date = LocalDate.now();
        return Day.builder()
                .date(date)
                .year(date.getYear())
                .month(date.getMonthValue())
                .build();
    }

}