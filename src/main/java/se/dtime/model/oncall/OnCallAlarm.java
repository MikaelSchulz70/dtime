package se.dtime.model.oncall;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnCallAlarm {
    private Long id;
    private String userName;
    private String projectName;
    private String companyName;
    private String sender;
    private String subject;
    private boolean emailSent;
    private boolean instantMsgSent;
    private OnCallStatus status;
    private String dateTime;
    private String message;
    private OnCallSeverity onCallSeverity;
}
