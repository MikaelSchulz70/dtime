package se.dtime.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    @NotNull
    @Size(min=1, max=20, message="User name should have between 1-20 characters")
    private String userName;

    @NotNull
    @Size(min=6, max=80, message="Password should have between 6-80 characters")
    private String password;

    @NotNull
    @Size(min=1, max=30, message="First name should have between 1-30 characters")
    private String firstName;

    @NotNull
    @Size(min=1, max=30, message="Last name should have between 1-30 characters")
    private String lastName;

    @NotNull
    @Size(min=1, max=60, message="Email should have between 1-60 characters")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Invalid user role")
    private UserRole userRole;

    @NotNull(message = "Invalid status")
    private ActivationStatus activationStatus;

    @NotNull(message = "Invalid user category")
    private UserCategory userCategory;

    @NotNull(message = "Invalid mobile number")
    @Size(min=10, max=20, message="Mobil number has invalid lenth 10-20 characters")
    @Pattern(regexp = "((\\+)(\\d{10,19}))", message = "Invalid mobile number. Ex. +46 733 34 00 00")
    private String mobileNumber;

    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;
}
