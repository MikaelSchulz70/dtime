package se.dtime.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotNull
    @Size(min = 6, max = 80, message = "Password should have between 6-80 characters")
    private String password;

    @NotNull
    @Size(min = 1, max = 30, message = "First name should have between 1-30 characters")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 30, message = "Last name should have between 1-30 characters")
    private String lastName;

    @NotNull
    @Size(min = 1, max = 60, message = "Email should have between 1-60 characters")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Invalid user role")
    private UserRole userRole;

    @NotNull(message = "Invalid status")
    private ActivationStatus activationStatus;

    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public ActivationStatus getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(ActivationStatus activationStatus) {
        this.activationStatus = activationStatus;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(LocalDateTime updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}
