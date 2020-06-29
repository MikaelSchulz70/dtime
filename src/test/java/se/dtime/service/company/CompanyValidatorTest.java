package se.dtime.service.company;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Company;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CompanyRepository;
import se.dtime.repository.ProjectRepository;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CompanyValidatorTest {
    @InjectMocks
    private CompanyValidator companyValidator;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setup() {
        companyValidator.init();

        CompanyPO companyPO = new CompanyPO();
        companyPO.setId(2L);
        when(companyRepository.findByName("test")).thenReturn(companyPO);
    }

    @Test
    public void validateUserNameOk() {
        Attribute attribute = Attribute.builder().id(0).name(CompanyValidator.FIELD_NAME).value("test1").build();
        companyValidator.validate(attribute);
    }

    @Test
    public void validateAddNameExists() {
        when(companyRepository.findByName("test")).thenReturn(new CompanyPO(2L));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.name.not.unique");
        companyValidator.validateAdd(Company.builder().id(1L).name("test").build());
    }

    @Test
    public void validateUpdateNotFound() {
        when(companyRepository.existsById(1L)).thenReturn(false);
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("company.not.found");
        companyValidator.validateUpdate(Company.builder().id(1L).build());
    }

    @Test
    public void validateUpdateNameExists() {
        when(companyRepository.existsById(1L)).thenReturn(true);
        when(companyRepository.findByName("test")).thenReturn(new CompanyPO(2L));
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.name.not.unique");
        companyValidator.validateUpdate(Company.builder().id(1L).name("test").build());
    }

    @Test
    public void validateUpdateNameInactive() {
        when(companyRepository.existsById(1L)).thenReturn(true);

        ProjectPO projectPO = new ProjectPO(3L);
        projectPO.setActivationStatus(ActivationStatus.ACTIVE);
        when(projectRepository.findByCompany(any(CompanyPO.class))).thenReturn(Arrays.asList(projectPO));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.inactivation.not.allowed");
        companyValidator.validateUpdate(Company.builder().id(1L).activationStatus(ActivationStatus.INACTIVE).build());
    }

    @Test
    public void validateUserNameLessThanMinLength() {
        Attribute attribute = Attribute.builder().id(0).name(CompanyValidator.FIELD_NAME).value("").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.name.length");
        companyValidator.validate(attribute);
    }

    @Test
    public void validateUserNameGrThanMax() {
        Attribute attribute = Attribute.builder().id(0).name(CompanyValidator.FIELD_NAME).value("01234567890123456789012345678901234567890").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.name.length");
        companyValidator.validate(attribute);
    }

    @Test
    public void validateUserNameNotUnique() {
        Attribute attribute = Attribute.builder().id(1).name(CompanyValidator.FIELD_NAME).value("test").build();
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.name.not.unique");
        companyValidator.validate(attribute);
    }

    @Test
    public void validateDeleteNotFound() {
        expectedException.expect(NotFoundException.class);
        expectedException.expectMessage("company.not.found");
        companyValidator.validateDelete(1l);
    }

    @Test
    public void validateDeleteHasActiveProject() {
        CompanyPO companyPO = new CompanyPO(1L);
        when(companyRepository.findById(1l)).thenReturn(Optional.of(companyPO));
        when(projectRepository.findByCompany(companyPO)).thenReturn(Arrays.asList(new ProjectPO(1L)));

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("company.cannot.delete.company.with.projects");
        companyValidator.validateDelete(1l);
    }
}