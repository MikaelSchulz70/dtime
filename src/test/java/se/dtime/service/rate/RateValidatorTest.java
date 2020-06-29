package se.dtime.service.rate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.Rate;
import se.dtime.model.UserCategory;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.AssignmentRepository;
import se.dtime.repository.RateRepository;
import se.dtime.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateValidatorTest {

    @InjectMocks
    private RateValidator rateValidator;
    @Mock
    private RateRepository rateRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        AssignmentPO assignmentPO = new AssignmentPO(1);
        ProjectPO projectPO = new ProjectPO();
        projectPO.setFixRate(false);
        assignmentPO.setProject(projectPO);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        UserPO userPO = new UserPO(2L);
        userPO.setUserCategory(UserCategory.EMPLOYEE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(userPO));
    }

    @Test
    public void validateAssignmentNotFound1() {
        Rate rate = createRate();
        rate.setIdAssignment(0);
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("assignment.not.found");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateAssignmentNotFound2() {
        Rate rate = createRate();
        rate.setIdAssignment(3L);
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("assignment.not.found");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateUsertNotFound1() {
        Rate rate = createRate();
        rate.setIdUser(0);
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("user.not.found");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateUserNotFound2() {
        Rate rate = createRate();
        rate.setIdUser(3L);
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("user.not.found");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateSubcontractorRate() {
        Rate rate = createRate();

        UserPO userPO = new UserPO(2L);
        userPO.setUserCategory(UserCategory.SUBCONTRACTOR);
        when(userRepository.findById(2L)).thenReturn(Optional.of(userPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.no.subcontractor.rate");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateEmployeeCannotHaveSubcontractorRate() {
        Rate rate = createRate();
        rate.setSubcontractorRate(BigDecimal.ONE);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.only.subcontractor.can.have.subcontractor.rate");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateNoCustomerRate1() {
        Rate rate = createRate();
        rate.setCustomerRate(null);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.customer.rate.not.specified");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateNoFromDate() {
        Rate rate = createRate();
        rate.setFromDate(null);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.from.date.not.specified");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateToDateAfterFromDate() {
        Rate rate = createRate();
        rate.setFromDate(rate.getToDate().plusDays(1));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.from.date.is.after.to.date");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateAlreadyActiveRate() {
        Rate rate = createRate();
        rate.setToDate(null);

        when(rateRepository.findByAssignmentOrderByFromDateDesc(any(AssignmentPO.class))).thenReturn(Collections.singletonList(new RatePO(1L)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.already.an.active.rate");
        rateValidator.validateAdd(rate);
    }

    @Test
    public void validateFromDateEqualsFromDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now());

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateFromDateEqualsToDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now().plusDays(5));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateFromDateBetweenToAndFromDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now().minusDays(1));
        ratePO.setToDate(LocalDate.now().plusDays(1));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    @Test
    public void validateToDateEqualsFromDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now().plusDays(5));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    @Test
    public void validateHourRateOnFixRateProject() {
        Rate rate = createRate();

        AssignmentPO assignmentPO = new AssignmentPO(1);
        ProjectPO projectPO = new ProjectPO();
        projectPO.setFixRate(true);
        assignmentPO.setProject(projectPO);
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignmentPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.hour.rate.cannot.be.on.fix.rate.project");
        rateValidator.validateAdd(rate);
    }


    public void validateToDateEqualsToDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now().minusDays(5));
        ratePO.setToDate(LocalDate.now().plusDays(5));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateToDateBetweenToAndFromDate() {
        Rate rate = createRate();

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now().minusDays(2));
        ratePO.setToDate(LocalDate.now().plusDays(6));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("rate.overlapping.date.range");
        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateOkRange1() {
        Rate rate = createRate();
        rate.setFromDate(LocalDate.now().minusDays(5));
        rate.setToDate(LocalDate.now().minusDays(1));

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now());
        ratePO.setToDate(LocalDate.now().plusDays(5));

        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateOkRange2() {
        Rate rate = createRate();
        rate.setFromDate(LocalDate.now().minusDays(6));
        rate.setToDate(LocalDate.now().minusDays(8));

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now());
        ratePO.setToDate(LocalDate.now().plusDays(5));

        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    public void validateOkRange3() {
        Rate rate = createRate();
        rate.setFromDate(LocalDate.now().plusDays(6));
        rate.setToDate(null);

        RatePO ratePO = new RatePO(1L);
        ratePO.setFromDate(LocalDate.now());
        ratePO.setToDate(LocalDate.now().plusDays(5));

        rateValidator.validateDateRange(rate, Collections.singletonList(ratePO));
    }

    private Rate createRate() {
        Rate rate = new Rate();
        rate.setId(2);
        rate.setIdAssignment(1L);
        rate.setIdUser(2L);
        rate.setCustomerRate(BigDecimal.TEN);
        rate.setFromDate(LocalDate.now());
        rate.setToDate(LocalDate.now().plusDays(5));
        return rate;
    }
}