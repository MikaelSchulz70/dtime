package se.dtime.dbmodel.oncall;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.oncall.OnCallSeverity;
import se.dtime.model.oncall.OnCallStatus;

import javax.persistence.*;

@Entity(name = "OnCallAlarm")
@Table(name = "oncallalarm")
public class OnCallAlarmPO extends BasePO {
    private Long id;
    private UserPO user;
    private ProjectPO project;
    private String sender;
    private String subject;
    private boolean emailSent;
    private boolean instantMsgSent;
    private OnCallStatus status;
    private String message;
    private OnCallSeverity onCallSeverity;

    public OnCallAlarmPO() {

    }

    public OnCallAlarmPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_oncallalarm")
    @SequenceGenerator(name = "seq_oncallalarm", sequenceName = "seq_oncallalarm", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = true, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public UserPO getUser() {
        return user;
    }

    public void setUser(UserPO user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project", nullable = false, updatable = false)
    public ProjectPO getProject() {
        return project;
    }

    public void setProject(ProjectPO project) {
        this.project = project;
    }

    @Column(name = "sender", unique = false, nullable = true, length = 100)
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Column(name = "subject", unique = false, nullable = true, length = 100)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "emailsent")
    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "instantmsgsent")
    public boolean isInstantMsgSent() {
        return instantMsgSent;
    }

    public void setInstantMsgSent(boolean instantMsgSent) {
        this.instantMsgSent = instantMsgSent;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false, length = 20)
    public OnCallStatus getStatus() {
        return status;
    }

    public void setStatus(OnCallStatus status) {
        this.status = status;
    }

    @Column(name = "message", unique = true, nullable = false, length = 250)
    public String getMessage() {
        return message;
    }

    public void setMessage(String errorMessage) {
        this.message = errorMessage;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", unique = false, nullable = false, length = 20)
    public OnCallSeverity getOnCallSeverity() {
        return onCallSeverity;
    }

    public void setOnCallSeverity(OnCallSeverity onCallSeverity) {
        this.onCallSeverity = onCallSeverity;
    }
}
