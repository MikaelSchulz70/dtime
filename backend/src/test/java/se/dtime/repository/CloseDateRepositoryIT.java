package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = {LiquibaseAutoConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
class CloseDateRepositoryIT {

    @Autowired
    private CloseDateRepository closeDateRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindCloseDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        LocalDate closeDate = LocalDate.of(2024, 12, 31);
        
        CloseDatePO closeDatePO = createCloseDate(user, closeDate);
        
        CloseDatePO saved = closeDateRepository.save(closeDatePO);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getDate()).isEqualTo(closeDate);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByUserAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        LocalDate closeDate = LocalDate.of(2024, 6, 30);
        
        CloseDatePO closeDatePO = createCloseDate(user, closeDate);
        closeDateRepository.save(closeDatePO);
        
        CloseDatePO found = closeDateRepository.findByUserAndDate(user, closeDate);
        
        assertThat(found).isNotNull();
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getDate()).isEqualTo(closeDate);
    }

    @Test
    void shouldReturnNullWhenCloseDateNotFoundByUserAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        LocalDate nonExistentDate = LocalDate.of(2024, 1, 1);
        
        CloseDatePO found = closeDateRepository.findByUserAndDate(user, nonExistentDate);
        
        assertThat(found).isNull();
    }

    @Test
    void shouldFindByUser() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        
        LocalDate date1 = LocalDate.of(2024, 3, 31);
        LocalDate date2 = LocalDate.of(2024, 6, 30);
        LocalDate date3 = LocalDate.of(2024, 9, 30);
        
        CloseDatePO closeDate1 = createCloseDate(user1, date1);
        CloseDatePO closeDate2 = createCloseDate(user1, date2);
        CloseDatePO closeDate3 = createCloseDate(user2, date3);
        
        closeDateRepository.save(closeDate1);
        closeDateRepository.save(closeDate2);
        closeDateRepository.save(closeDate3);
        
        List<CloseDatePO> user1CloseDates = closeDateRepository.findByUser(user1);
        
        assertThat(user1CloseDates).hasSize(2);
        assertThat(user1CloseDates).allMatch(cd -> cd.getUser().getId().equals(user1.getId()));
        assertThat(user1CloseDates).extracting(CloseDatePO::getDate)
            .containsExactlyInAnyOrder(date1, date2);
        
        List<CloseDatePO> user2CloseDates = closeDateRepository.findByUser(user2);
        
        assertThat(user2CloseDates).hasSize(1);
        assertThat(user2CloseDates.get(0).getDate()).isEqualTo(date3);
    }

    @Test
    void shouldReturnEmptyListWhenNoCloseDatesFoundForUser() {
        UserPO user = createAndSaveUser("empty@example.com", "Empty", "User");
        
        List<CloseDatePO> closeDates = closeDateRepository.findByUser(user);
        
        assertThat(closeDates).isEmpty();
    }

    @Test
    void shouldDeleteByUserAndDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        LocalDate closeDate = LocalDate.of(2024, 12, 31);
        
        CloseDatePO closeDatePO = createCloseDate(user, closeDate);
        closeDateRepository.save(closeDatePO);
        
        // Verify it exists
        CloseDatePO found = closeDateRepository.findByUserAndDate(user, closeDate);
        assertThat(found).isNotNull();
        
        // Delete it
        closeDateRepository.deleteByUserAndDate(user, closeDate);
        
        // Verify it's deleted
        CloseDatePO notFound = closeDateRepository.findByUserAndDate(user, closeDate);
        assertThat(notFound).isNull();
    }

    @Test
    void shouldDeleteOnlySpecificUserAndDate() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        LocalDate date1 = LocalDate.of(2024, 6, 30);
        LocalDate date2 = LocalDate.of(2024, 12, 31);
        
        CloseDatePO closeDate1 = createCloseDate(user1, date1);
        CloseDatePO closeDate2 = createCloseDate(user1, date2);
        CloseDatePO closeDate3 = createCloseDate(user2, date1);
        
        closeDateRepository.save(closeDate1);
        closeDateRepository.save(closeDate2);
        closeDateRepository.save(closeDate3);
        
        // Delete only user1's date1
        closeDateRepository.deleteByUserAndDate(user1, date1);
        
        // Verify specific deletion
        assertThat(closeDateRepository.findByUserAndDate(user1, date1)).isNull();
        assertThat(closeDateRepository.findByUserAndDate(user1, date2)).isNotNull();
        assertThat(closeDateRepository.findByUserAndDate(user2, date1)).isNotNull();
    }

    @Test
    void shouldHandleMultipleCloseDatesForSameUser() {
        UserPO user = createAndSaveUser("user@example.com", "Multi", "User");
        
        LocalDate[] dates = {
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2024, 2, 29),
            LocalDate.of(2024, 3, 31),
            LocalDate.of(2024, 4, 30),
            LocalDate.of(2024, 5, 31)
        };
        
        for (LocalDate date : dates) {
            CloseDatePO closeDate = createCloseDate(user, date);
            closeDateRepository.save(closeDate);
        }
        
        List<CloseDatePO> userCloseDates = closeDateRepository.findByUser(user);
        
        assertThat(userCloseDates).hasSize(5);
        assertThat(userCloseDates).extracting(CloseDatePO::getDate)
            .containsExactlyInAnyOrder(dates);
    }

    @Test
    void shouldDeleteCloseDate() {
        UserPO user = createAndSaveUser("user@example.com", "Test", "User");
        LocalDate closeDate = LocalDate.of(2024, 12, 31);
        
        CloseDatePO closeDatePO = createCloseDate(user, closeDate);
        CloseDatePO saved = closeDateRepository.save(closeDatePO);
        
        closeDateRepository.delete(saved);
        
        CloseDatePO found = closeDateRepository.findByUserAndDate(user, closeDate);
        assertThat(found).isNull();
    }

    @Test
    void shouldFindAllCloseDates() {
        UserPO user1 = createAndSaveUser("user1@example.com", "User", "One");
        UserPO user2 = createAndSaveUser("user2@example.com", "User", "Two");
        
        CloseDatePO closeDate1 = createCloseDate(user1, LocalDate.of(2024, 6, 30));
        CloseDatePO closeDate2 = createCloseDate(user2, LocalDate.of(2024, 12, 31));
        
        closeDateRepository.save(closeDate1);
        closeDateRepository.save(closeDate2);
        
        List<CloseDatePO> allCloseDates = closeDateRepository.findAll();
        
        assertThat(allCloseDates).hasSize(2);
        assertThat(allCloseDates).extracting(cd -> cd.getUser().getEmail())
            .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    private CloseDatePO createCloseDate(UserPO user, LocalDate date) {
        CloseDatePO closeDate = new CloseDatePO();
        closeDate.setUser(user);
        closeDate.setDate(date);
        closeDate.setCreatedBy(1L);
        closeDate.setUpdatedBy(1L);
        closeDate.setCreateDateTime(LocalDateTime.now());
        closeDate.setUpdatedDateTime(LocalDateTime.now());
        return closeDate;
    }

    private UserPO createAndSaveUser(String email, String firstName, String lastName) {
        UserPO user = new UserPO();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("password123");
        user.setUserRole(UserRole.USER);
        user.setActivationStatus(ActivationStatus.ACTIVE);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setCreateDateTime(LocalDateTime.now());
        user.setUpdatedDateTime(LocalDateTime.now());
        return userRepository.save(user);
    }
}