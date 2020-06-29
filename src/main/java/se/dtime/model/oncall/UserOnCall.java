package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserOnCall {
    private String name;
    private String email;
    private String mobileNumber;
}
