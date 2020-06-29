package se.dtime.model.oncall;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OnCallUser {
    private long idUser;
    private String userName;
    private String email;
    private String mobileNumber;
    private List<OnCallDay> onCallDays;
}
