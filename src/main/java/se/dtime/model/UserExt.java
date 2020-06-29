package se.dtime.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserExt extends User {
    private long id;
    private String firstName;
    private String lastName;

    public UserExt(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public UserExt(String username, String password, Collection<? extends GrantedAuthority> authorities,
                   long id, String firstName, String lastName) {
        super(username, password, authorities);

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
