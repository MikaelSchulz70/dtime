package se.dtime.dbmodel.oncall;

import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.BasePO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name="OnCall")
@Table(name = "oncall")
@NamedQueries({
        @NamedQuery(name = "OnCallPO.findByBetweenDates", query = "SELECT oc FROM OnCall oc WHERE date >= :startDate and date <= :endDate ORDER BY oc.date"),
        @NamedQuery(name = "OnCallPO.findByAssignmentAndDate", query = "SELECT oc FROM OnCall oc WHERE oc.assignment.id = :idAssignment AND date =:date"),
        @NamedQuery(name = "OnCallPO.findByProject", query = "SELECT oc FROM OnCall oc WHERE oc.assignment.project.id = :idProject"),
        @NamedQuery(name = "OnCallPO.findByProjectAndDate", query = "SELECT oc FROM OnCall oc WHERE oc.assignment.project.id = :idProject AND date =:date")
})
public class OnCallPO extends BasePO {
    private Long id;
    private LocalDate date;
    private AssignmentPO assignment;

    public OnCallPO() {

    }

    public OnCallPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq_oncall")
    @SequenceGenerator(name="seq_oncall", sequenceName="seq_oncall", allocationSize=1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "date", unique = false, nullable = false, updatable=false)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_assignment", nullable = false)
    public AssignmentPO getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentPO assignment) {
        this.assignment = assignment;
    }
}
