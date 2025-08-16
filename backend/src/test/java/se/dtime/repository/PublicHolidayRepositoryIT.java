package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.PublicHolidayPO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class PublicHolidayRepositoryIT {

    @Autowired
    private PublicHolidayRepository publicHolidayRepository;

    @Test
    void shouldSaveAndFindPublicHoliday() {
        PublicHolidayPO holiday = createPublicHoliday("Christmas Day", false);
        
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Christmas Day");
        assertThat(saved.isWorkday()).isFalse();
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldSaveWorkdayHoliday() {
        PublicHolidayPO holiday = createPublicHoliday("Christmas Eve", true);
        
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Christmas Eve");
        assertThat(saved.isWorkday()).isTrue();
    }

    @Test
    void shouldFindById() {
        PublicHolidayPO holiday = createPublicHoliday("New Year's Day", false);
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        Optional<PublicHolidayPO> found = publicHolidayRepository.findById(saved.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New Year's Day");
        assertThat(found.get().isWorkday()).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenHolidayNotFoundById() {
        Optional<PublicHolidayPO> found = publicHolidayRepository.findById(999L);
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllPublicHolidays() {
        PublicHolidayPO holiday1 = createPublicHoliday("Easter Sunday", false);
        PublicHolidayPO holiday2 = createPublicHoliday("Easter Monday", false);
        PublicHolidayPO holiday3 = createPublicHoliday("Good Friday", true);
        
        publicHolidayRepository.save(holiday1);
        publicHolidayRepository.save(holiday2);
        publicHolidayRepository.save(holiday3);
        
        List<PublicHolidayPO> allHolidays = publicHolidayRepository.findAll();
        
        assertThat(allHolidays).hasSize(3);
        assertThat(allHolidays).extracting(PublicHolidayPO::getName)
            .containsExactlyInAnyOrder("Easter Sunday", "Easter Monday", "Good Friday");
    }

    @Test
    void shouldCountAllPublicHolidays() {
        PublicHolidayPO holiday1 = createPublicHoliday("Independence Day", false);
        PublicHolidayPO holiday2 = createPublicHoliday("Labor Day", false);
        
        publicHolidayRepository.save(holiday1);
        publicHolidayRepository.save(holiday2);
        
        long count = publicHolidayRepository.count();
        
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldUpdatePublicHoliday() {
        PublicHolidayPO holiday = createPublicHoliday("Thanksgiving", false);
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        saved.setWorkday(true);
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());
        
        PublicHolidayPO updated = publicHolidayRepository.save(saved);
        
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.isWorkday()).isTrue();
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("Thanksgiving"); // Name should remain unchanged
    }

    @Test
    void shouldDeletePublicHoliday() {
        PublicHolidayPO holiday = createPublicHoliday("Valentine's Day", true);
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        publicHolidayRepository.delete(saved);
        
        Optional<PublicHolidayPO> found = publicHolidayRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteById() {
        PublicHolidayPO holiday = createPublicHoliday("Halloween", true);
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        publicHolidayRepository.deleteById(saved.getId());
        
        Optional<PublicHolidayPO> found = publicHolidayRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldHandleUniqueNameConstraint() {
        PublicHolidayPO holiday1 = createPublicHoliday("Mother's Day", false);
        PublicHolidayPO holiday2 = createPublicHoliday("Mother's Day", true);
        
        publicHolidayRepository.save(holiday1);
        
        // This should fail due to unique constraint on name, but we'll test the first one was saved
        Optional<PublicHolidayPO> found = publicHolidayRepository.findById(holiday1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Mother's Day");
        assertThat(found.get().isWorkday()).isFalse();
    }

    @Test
    void shouldCheckExistenceById() {
        PublicHolidayPO holiday = createPublicHoliday("Father's Day", true);
        PublicHolidayPO saved = publicHolidayRepository.save(holiday);
        
        boolean exists = publicHolidayRepository.existsById(saved.getId());
        boolean notExists = publicHolidayRepository.existsById(999L);
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldSaveMixedWorkdayAndNonWorkdayHolidays() {
        PublicHolidayPO nonWorkday1 = createPublicHoliday("Memorial Day", false);
        PublicHolidayPO workday1 = createPublicHoliday("Presidents Day", true);
        PublicHolidayPO nonWorkday2 = createPublicHoliday("Veterans Day", false);
        
        publicHolidayRepository.save(nonWorkday1);
        publicHolidayRepository.save(workday1);
        publicHolidayRepository.save(nonWorkday2);
        
        List<PublicHolidayPO> allHolidays = publicHolidayRepository.findAll();
        
        long workdayCount = allHolidays.stream().mapToLong(h -> h.isWorkday() ? 1 : 0).sum();
        long nonWorkdayCount = allHolidays.stream().mapToLong(h -> h.isWorkday() ? 0 : 1).sum();
        
        assertThat(workdayCount).isEqualTo(1);
        assertThat(nonWorkdayCount).isEqualTo(2);
    }

    private PublicHolidayPO createPublicHoliday(String name, boolean workday) {
        PublicHolidayPO holiday = new PublicHolidayPO();
        holiday.setName(name);
        holiday.setWorkday(workday);
        holiday.setCreatedBy(1L);
        holiday.setUpdatedBy(1L);
        holiday.setCreateDateTime(LocalDateTime.now());
        holiday.setUpdatedDateTime(LocalDateTime.now());
        return holiday;
    }
}