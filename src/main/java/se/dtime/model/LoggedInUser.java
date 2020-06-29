package se.dtime.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoggedInUser {
    private long idUser;
    private String name;
    private boolean isAdmin;
}
