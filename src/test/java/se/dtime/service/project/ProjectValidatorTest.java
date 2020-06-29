package se.dtime.service.project;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.RatePO;
import se.dtime.model.Attribute;
import se.dtime.model.Company;
import se.dtime.model.Project;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.FixRateRepository;
import se.dtime.repository.ProjectRepository;
import se.dtime.repository.RateRepository;
import se.dtime.repository.TimeReportRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectValidatorTest {
    @InjectMocks
    private ProjectValidator projectValidator;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TimeReportRepository timeReportRepository;
    @Mock
    private RateRepository rateRepository;
    @Mock
    private FixRateRepository fixRateRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        projectValidator.init();

        ProjectPO projectPO = new ProjectPO();
        projectPO.setId(2L);
        when(projectRepository.findByName("test")).thenReturn(Arrays.asList(projectPO));
    }

    @Test
    public void validateUserNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(ProjectValidator.FIELD_NAME).value("test1").build();
        projectValidator.validate(attribute);
    }

    @Test
    public void validateUserNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(ProjectValidator.FIELD_NAME).value("").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.name.length");
        projectValidator.validate(attribute);
    }

    @Test
    public void validateNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(ProjectValidator.FIELD_NAME).value("01234567890123456789012345678901234567890").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.name.length");
        projectValidator.validate(attribute);
    }

    @Test
    public void validateAddNameExist() {
        Project project = Project.builder().id(1L).name("test").company(Company.builder().id(1L).build()).build();

        ProjectPO projectPO = new ProjectPO(2L);
        CompanyPO companyPO = new CompanyPO(2L);
        projectPO.setCompany(companyPO);

        when(projectRepository.findByName("test")).thenReturn(Arrays.asList());
        projectValidator.validateAdd(project);

        when(projectRepository.findByName("test")).thenReturn(Arrays.asList(projectPO));
        projectValidator.validateAdd(project);

        companyPO.setId(1L);
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.name.not.unique");
        projectValidator.validateAdd(project);
    }

    @Test
    public void validateUpdateNotFound() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("project.not.found");
        projectValidator.validateUpdate(Project.builder().id(1L).build());
    }

    @Test
    public void validateUpdateNameExist() {
        Project project = Project.builder().id(1L).name("test").company(Company.builder().id(1L).build()).build();

        ProjectPO projectPO = new ProjectPO(2L);
        CompanyPO companyPO = new CompanyPO(2L);
        projectPO.setCompany(companyPO);

        when(projectRepository.findByName("test")).thenReturn(Arrays.asList());
        projectValidator.validateAdd(project);

        when(projectRepository.findByName("test")).thenReturn(Arrays.asList(projectPO));
        projectValidator.validateAdd(project);

        companyPO.setId(1L);
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.name.not.unique");
        projectValidator.validateAdd(project);
    }

    @Test
    public void validateDeleteProjectNotFound() {
        expectedException.expectMessage("project.not.found");
        expectedException.expect(NotFoundException.class);
        projectValidator.validateDelete(1L);
    }

    @Test
    public void validateDeleteProjectHasAssignments() {
        ProjectPO projectPO = new ProjectPO(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        when(timeReportRepository.sumReportedTimeByProject(1L)).thenReturn(BigDecimal.TEN);

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.cannot.delete.have.time.reports");
        projectValidator.validateDelete(1L);
    }


    @Test
    public void validateDeleteOk() {
        ProjectPO projectPO = new ProjectPO(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(projectPO));
        when(timeReportRepository.sumReportedTimeByProject(1L)).thenReturn(BigDecimal.ZERO);
        projectValidator.validateDelete(1L);
    }

    @Test
    public void validateRateOkTest() {
        Project project = new Project();
        project.setFixRate(true);
        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setFixRate(true);
        projectValidator.validateRate(project, projectPO);

        project.setFixRate(false);
        when(rateRepository.findByProject(1L)).thenReturn(createRates(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 12, 21)));
        when(fixRateRepository.findByProject(projectPO)).thenReturn(createFixRates(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 9, 30)));

        projectValidator.validateRate(project, projectPO);

        project.setFixRate(true);
        projectPO.setFixRate(false);
        projectValidator.validateRate(project, projectPO);
    }

    @Test
    public void validateRateHasOpenEndTest() {
        Project project = new Project();
        project.setFixRate(true);
        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setFixRate(false);

        when(rateRepository.findByProject(1L)).thenReturn(createRates(LocalDate.of(2019, 10, 1), null));
        when(fixRateRepository.findByProject(projectPO)).thenReturn(createFixRates(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 9, 30)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.open.rate");
        projectValidator.validateRate(project, projectPO);
    }

    @Test
    public void validateFixRateHasOpenEndTest() {
        Project project = new Project();
        project.setFixRate(false);
        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setFixRate(true);

        when(rateRepository.findByProject(1L)).thenReturn(createRates(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 30)));
        when(fixRateRepository.findByProject(projectPO)).thenReturn(createFixRates(LocalDate.of(2019, 8, 1), null));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.open.fix.rate");
        projectValidator.validateRate(project, projectPO);
    }

    @Test
    public void validateFixRateOverlappingTest() {
        Project project = new Project();
        project.setFixRate(false);
        ProjectPO projectPO = new ProjectPO(1L);
        projectPO.setFixRate(true);

        when(rateRepository.findByProject(1L)).thenReturn(createRates(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 30)));
        when(fixRateRepository.findByProject(projectPO)).thenReturn(createFixRates(LocalDate.of(2019, 8, 1), LocalDate.of(2019, 10, 30)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("project.overlapping.rates");
        projectValidator.validateRate(project, projectPO);
    }

    private List<FixRatePO> createFixRates(LocalDate startDate, LocalDate toDate) {
        FixRatePO fixRatePO = new FixRatePO();
        fixRatePO.setFromDate(startDate);
        fixRatePO.setToDate(toDate);
        return Collections.singletonList(fixRatePO);
    }

    private List<RatePO> createRates(LocalDate startDate, LocalDate toDate) {
        RatePO ratePO = new RatePO();
        ratePO.setFromDate(startDate);
        ratePO.setToDate(toDate);
        return Collections.singletonList(ratePO);
    }
}