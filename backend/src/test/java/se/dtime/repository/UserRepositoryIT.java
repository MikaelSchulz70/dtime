package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        UserPO user = createUser("john@example.com", "John", "Doe", UserRole.USER, ActivationStatus.ACTIVE);

        UserPO saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getUserRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByEmail() {
        UserPO user = createUser("findme@example.com", "Find", "Me", UserRole.USER, ActivationStatus.ACTIVE);
        userRepository.save(user);

        UserPO found = userRepository.findByEmail("findme@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("findme@example.com");
        assertThat(found.getFirstName()).isEqualTo("Find");
        assertThat(found.getLastName()).isEqualTo("Me");
    }

    @Test
    void shouldReturnNullWhenUserNotFoundByEmail() {
        UserPO found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isNull();
    }

    @Test
    void shouldFindByActivationStatusOrderByFirstNameAsc() {
        UserPO activeUser1 = createUser("charlie@example.com", "Charlie", "Brown", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO activeUser2 = createUser("alice@example.com", "Alice", "Smith", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO inactiveUser = createUser("bob@example.com", "Bob", "Jones", UserRole.USER, ActivationStatus.INACTIVE);

        userRepository.save(activeUser1);
        userRepository.save(activeUser2);
        userRepository.save(inactiveUser);

        List<UserPO> activeUsers = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);

        assertThat(activeUsers).hasSize(2);
        assertThat(activeUsers.get(0).getFirstName()).isEqualTo("Alice");
        assertThat(activeUsers.get(1).getFirstName()).isEqualTo("Charlie");
        assertThat(activeUsers).allMatch(user -> user.getActivationStatus() == ActivationStatus.ACTIVE);
    }

    @Test
    void shouldFindInactiveUsersByStatus() {
        UserPO activeUser = createUser("active@example.com", "Active", "User", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO inactiveUser = createUser("inactive@example.com", "Inactive", "User", UserRole.USER, ActivationStatus.INACTIVE);

        userRepository.save(activeUser);
        userRepository.save(inactiveUser);

        List<UserPO> inactiveUsers = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.INACTIVE);

        assertThat(inactiveUsers).hasSize(1);
        assertThat(inactiveUsers.get(0).getFirstName()).isEqualTo("Inactive");
        assertThat(inactiveUsers.get(0).getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
    }

    @Test
    void shouldFindByUserRoleAndActivationStatus() {
        UserPO adminUser = createUser("admin@example.com", "Admin", "User", UserRole.ADMIN, ActivationStatus.ACTIVE);
        UserPO regularUser = createUser("user@example.com", "Regular", "User", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO inactiveAdmin = createUser("inactive.admin@example.com", "Inactive", "Admin", UserRole.ADMIN, ActivationStatus.INACTIVE);

        userRepository.save(adminUser);
        userRepository.save(regularUser);
        userRepository.save(inactiveAdmin);

        List<UserPO> activeAdmins = userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE);

        assertThat(activeAdmins).hasSize(1);
        assertThat(activeAdmins.get(0).getEmail()).isEqualTo("admin@example.com");
        assertThat(activeAdmins.get(0).getUserRole()).isEqualTo(UserRole.ADMIN);
        assertThat(activeAdmins.get(0).getActivationStatus()).isEqualTo(ActivationStatus.ACTIVE);

        List<UserPO> activeUsers = userRepository.findByUserRoleAndActivationStatus(UserRole.USER, ActivationStatus.ACTIVE);

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("user@example.com");
        assertThat(activeUsers.get(0).getUserRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersFoundByRoleAndStatus() {
        List<UserPO> users = userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE);

        assertThat(users).isEmpty();
    }

    @Test
    void shouldUpdateUser() {
        UserPO user = createUser("update@example.com", "Update", "Test", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO saved = userRepository.save(user);

        saved.setActivationStatus(ActivationStatus.INACTIVE);
        saved.setUserRole(UserRole.ADMIN);
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());

        UserPO updated = userRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getActivationStatus()).isEqualTo(ActivationStatus.INACTIVE);
        assertThat(updated.getUserRole()).isEqualTo(UserRole.ADMIN);
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    void shouldDeleteUser() {
        UserPO user = createUser("delete@example.com", "Delete", "Test", UserRole.USER, ActivationStatus.ACTIVE);
        UserPO saved = userRepository.save(user);

        userRepository.delete(saved);

        UserPO found = userRepository.findByEmail("delete@example.com");
        assertThat(found).isNull();
    }

    @Test
    void shouldGetFullName() {
        UserPO user = createUser("fullname@example.com", "John", "Smith", UserRole.USER, ActivationStatus.ACTIVE);

        assertThat(user.getFullName()).isEqualTo("John Smith");
    }

    private UserPO createUser(String email, String firstName, String lastName, UserRole userRole, ActivationStatus status) {
        UserPO user = new UserPO();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("password123");
        user.setUserRole(userRole);
        user.setActivationStatus(status);
        user.setCreatedBy(1L);
        user.setUpdatedBy(1L);
        user.setCreateDateTime(LocalDateTime.now());
        user.setUpdatedDateTime(LocalDateTime.now());
        return user;
    }
}