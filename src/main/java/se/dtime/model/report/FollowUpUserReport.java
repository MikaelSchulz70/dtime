package se.dtime.model.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowUpUserReport extends FollowUpBaseReport {
    private long idUser;
    private String userName;
}
