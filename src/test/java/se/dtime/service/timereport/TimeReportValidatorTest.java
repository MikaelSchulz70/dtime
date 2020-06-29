package se.dtime.service.timereport;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.error.InvalidInputException;
import se.dtime.model.error.ValidationException;
import se.dtime.model.timereport.Day;
import se.dtime.model.timereport.TimeReportDay;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.CloseDateRepository;
import se.dtime.service.calendar.CalendarService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeReportValidatorTest {
    @InjectMocks
    private TimeReportValidator timeReportValidator;
    @Mock
    private CalendarService calendarService;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private CloseDateRepository closeDateRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
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

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("time.not.a.number");
        timeReportValidator.validate(time);
    }

    @Test
    public void validateTimeNegative() {
        Attribute time = Attribute.builder().id(1).name("time").value("-1").build();

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("time.not.within.valid.span");
        timeReportValidator.validate(time);
    }

    @Test
    public void validateTimeToBig() {
        Attribute time = Attribute.builder().id(1).name("time").value("25").build();

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("time.not.within.valid.span");
        timeReportValidator.validate(time);
    }

    @Test
    public void testValidateYearMouthOk() {
        timeReportValidator.validateYearMouth(2018, 10);
    }

    @Test
    public void testValidateYearMouthInvalidYear() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouth(-1, 10);
    }

    @Test
    public void testValidateYearMouthInvalidMonth() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouth(2018, 14);
    }

    @Test
    public void testValidateYearWeekOk() {
        timeReportValidator.validateYearWeek(2018, 44);
    }

    @Test
    public void testValidateYearWeekInvalidYear() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearWeek(-1, 10);
    }

    @Test
    public void testValidateYearWeekInvalidWeek() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouth(2018, 53);
    }

    @Test
    public void testValidateYearMouthDayOk() {
        when(calendarService.getNumberOfDaysInMonth(2018, 10)).thenReturn(31);
        timeReportValidator.validateYearMouthDay(2018, 10, 8);
    }

    @Test
    public void testValidateYearMouthDayInvalidYear() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouthDay(-1, 10, 8);
    }

    @Test
    public void testValidateYearMouthDayInvalidMonth() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouthDay(2018, 0, 8);
    }

    @Test
    public void testValidateYearMouthDayInvalidDay() {
        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateYearMouthDay(2018, 10, 32);
    }

    @Test
    public void validateTimeReportDayAssignmentNotFound() {
        TimeReportDay timeReportDay = TimeReportDay.builder().idAssignment(1).build();

        expectedException.expect(ValidationException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayInactiveAssignment() {
        TimeReportDay timeReportDay = TimeReportDay.builder().idAssignment(1).build();

        AssignmentPO assignmentPO = new AssignmentPO(1);
        assignmentPO.setActivationStatus(ActivationStatus.INACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(ValidationException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayTimeTooSmall() {
        TimeReportDay timeReportDay = TimeReportDay.builder().idAssignment(1).time(-1f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayTimeTooLarge() {
        TimeReportDay timeReportDay = TimeReportDay.builder().idAssignment(1).time(25f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayTooManyDecimals() {
        TimeReportDay timeReportDay = TimeReportDay.builder().day(createDay()).idAssignment(1).time(8.112f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(InvalidInputException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayNoDecimals() {
        TimeReportDay timeReportDay = TimeReportDay.builder().day(createDay()).idAssignment(1).time(8.f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayNoDecimals1() {
        TimeReportDay timeReportDay = TimeReportDay.builder().day(createDay()).idAssignment(1).time(8f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayOneDecimal() {
        TimeReportDay timeReportDay = TimeReportDay.builder().day(createDay()).idAssignment(1).time(8.5f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportDayTwoDecimals() {
        TimeReportDay timeReportDay = TimeReportDay.builder().day(createDay()).idAssignment(1).time(8.55f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        timeReportValidator.validateAdd(timeReportDay);
    }

    @Test
    public void validateTimeReportClosed() {
        Day day = createDay();
        TimeReportDay timeReportDay = TimeReportDay.builder().day(day).idAssignment(1).time(8.f).build();

        AssignmentPO assignmentPO = new AssignmentPO();
        UserPO userPO = new UserPO(1L);
        assignmentPO.setUser(userPO);
        assignmentPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));
        when(closeDateRepository.findByUserAndDate(userPO, LocalDate.of(day.getYear(), day.getMonth(), 1))).thenReturn(new CloseDatePO());

        expectedException.expect(ValidationException.class);
        timeReportValidator.validateAdd(timeReportDay);
    }

    private Day createDay() {
        Day day = new Day();
        LocalDate date = LocalDate.now();
        day.setDate(date);
        day.setYear(date.getYear());
        day.setMonth(date.getMonthValue());
        return day;
    }

}