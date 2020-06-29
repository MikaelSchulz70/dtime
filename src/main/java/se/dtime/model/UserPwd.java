package se.dtime.model;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPwd {

    @NotNull
    @Size(min=6, max=80, message="Invalid current password")
    private String currentPassword;

    @NotNull
    @Size(min=6, max=80, message="Invalid new password")
    private String newPassword1;

    @NotNull
    @Size(min=6, max=80, message="Invalid new password")
    private String newPassword2;
}
