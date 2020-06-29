package se.dtime.dbmodel.oncall;

import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity(name="OnCallConfig")
@Table(name = "oncallconfig")
public class OnCallConfigPO extends BasePO {
    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private ProjectPO projectPO;
    private ActivationStatus activationStatus;

    public OnCallConfigPO() {

    }

    public OnCallConfigPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq_oncallconfig")
    @SequenceGenerator(name="seq_oncallconfig", sequenceName="seq_oncallconfig", allocationSize=1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "starttime", unique = false, nullable = false, updatable=true)
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Column(name = "endtime", unique = false, nullable = false, updatable=true)
    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project", nullable = false)
    public ProjectPO getProject() {
        return projectPO;
    }

    public void setProject(ProjectPO projectPO) {
        this.projectPO = projectPO;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "day", unique = false, nullable = false, length = 20)
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false, length = 20)
    public ActivationStatus getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(ActivationStatus activationStatus) {
        this.activationStatus = activationStatus;
    }

}
