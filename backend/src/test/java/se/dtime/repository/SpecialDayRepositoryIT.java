package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.timereport.DayType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=true;INIT=CREATE SCHEMA IF NOT EXISTS \"public\"",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC"
})
class SpecialDayRepositoryIT {

    @Autowired
    private SpecialDayRepository specialDayRepository;

    @Test
    void shouldSaveAndFindSpecialDay() {
        SpecialDayPO specialDay = createSpecialDay("Christmas Day", LocalDate.of(2025, 12, 24), DayType.PUBLIC_HOLIDAY);

        SpecialDayPO saved = specialDayRepository.save(specialDay);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Christmas Day");
        assertThat(saved.getDayType()).isEqualTo(DayType.PUBLIC_HOLIDAY);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldSaveWorkdayHoliday() {
        SpecialDayPO specialDay = createSpecialDay("Christmas Day", LocalDate.of(2025, 12, 24), DayType.PUBLIC_HOLIDAY);

        SpecialDayPO saved = specialDayRepository.save(specialDay);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Christmas Day");
        assertThat(saved.getDayType()).isEqualTo(DayType.PUBLIC_HOLIDAY);
    }

    @Test
    void shouldFindById() {
        SpecialDayPO specialDay = createSpecialDay("Test half Day", LocalDate.of(2025, 12, 24), DayType.HALF_DAY);
        SpecialDayPO saved = specialDayRepository.save(specialDay);

        Optional<SpecialDayPO> found = specialDayRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test half Day");
        assertThat(found.get().getDayType()).isEqualTo(DayType.HALF_DAY);
    }

    @Test
    void shouldReturnEmptyWhenHolidayNotFoundById() {
        Optional<SpecialDayPO> found = specialDayRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllSpecialDays() {
        SpecialDayPO specialDay1 = createSpecialDay("Easter Sunday", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO specialDay2 = createSpecialDay("Easter Monday", LocalDate.of(2025, 4, 21), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO specialDay3 = createSpecialDay("Good Friday", LocalDate.of(2025, 4, 18), DayType.HALF_DAY);

        specialDayRepository.save(specialDay1);
        specialDayRepository.save(specialDay2);
        specialDayRepository.save(specialDay3);

        List<SpecialDayPO> allHolidays = specialDayRepository.findAll();

        assertThat(allHolidays).hasSize(3);
        assertThat(allHolidays).extracting(SpecialDayPO::getName)
                .containsExactlyInAnyOrder("Easter Sunday", "Easter Monday", "Good Friday");
    }

    @Test
    void shouldCountAllSpecialDays() {
        SpecialDayPO specialDay1 = createSpecialDay("Independence Day", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO specialDay2 = createSpecialDay("Labor Day", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);

        specialDayRepository.save(specialDay1);
        specialDayRepository.save(specialDay2);

        long count = specialDayRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldUpdateSpecialDay() {
        SpecialDayPO specialDay = createSpecialDay("Thanksgiving", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO saved = specialDayRepository.save(specialDay);

        saved.setDayType(DayType.HALF_DAY);
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());

        SpecialDayPO updated = specialDayRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getDayType()).isEqualTo(DayType.HALF_DAY);
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("Thanksgiving"); // Name should remain unchanged
    }

    @Test
    void shouldDeleteSpecialDay() {
        SpecialDayPO specialDay = createSpecialDay("Valentine's Day", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO saved = specialDayRepository.save(specialDay);

        specialDayRepository.delete(saved);

        Optional<SpecialDayPO> found = specialDayRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteById() {
        SpecialDayPO specialDay = createSpecialDay("Halloween", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO saved = specialDayRepository.save(specialDay);

        specialDayRepository.deleteById(saved.getId());

        Optional<SpecialDayPO> found = specialDayRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistenceById() {
        SpecialDayPO specialDay = createSpecialDay("Father's Day", LocalDate.of(2025, 4, 20), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO saved = specialDayRepository.save(specialDay);

        boolean exists = specialDayRepository.existsById(saved.getId());
        boolean notExists = specialDayRepository.existsById(999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindByYear() {
        SpecialDayPO day2024 = createSpecialDay("Test 2024", LocalDate.of(2024, 5, 1), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO day2025 = createSpecialDay("Test 2025", LocalDate.of(2025, 6, 1), DayType.PUBLIC_HOLIDAY);
        SpecialDayPO day2025b = createSpecialDay("Test 2025b", LocalDate.of(2025, 7, 1), DayType.HALF_DAY);

        specialDayRepository.save(day2024);
        specialDayRepository.save(day2025);
        specialDayRepository.save(day2025b);

        List<SpecialDayPO> result = specialDayRepository.findByYear(2025);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SpecialDayPO::getName)
                .containsExactlyInAnyOrder("Test 2025", "Test 2025b");
    }

    private SpecialDayPO createSpecialDay(String name, LocalDate date, DayType dayType) {
        SpecialDayPO specialDayPO = new SpecialDayPO();
        specialDayPO.setName(name);
        specialDayPO.setDayType(dayType);
        specialDayPO.setDate(date);
        specialDayPO.setCreatedBy(1L);
        specialDayPO.setUpdatedBy(1L);
        specialDayPO.setCreateDateTime(LocalDateTime.now());
        specialDayPO.setUpdatedDateTime(LocalDateTime.now());
        return specialDayPO;
    }
}