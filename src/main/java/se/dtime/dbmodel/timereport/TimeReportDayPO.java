package se.dtime.dbmodel.timereport;

import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.BasePO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "TimeReportDay")
@Table(name = "timereport")
@NamedQueries({
        @NamedQuery(name = "TimeReportDayPO.findByUserAndDate", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.user.id = :idUser AND date=:date ORDER BY tr.date"),
        @NamedQuery(name = "TimeReportDayPO.findByUserAndBetweenDates", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.user.id = :idUser AND date >= :startDate and date <= :endDate ORDER BY tr.date"),
        @NamedQuery(name = "TimeReportDayPO.findByAssignmentAndDate", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.id = :idAssignment AND date =:date"),
        @NamedQuery(name = "TimeReportDayPO.countByUserId", query = "SELECT count(tr) FROM TimeReportDay tr WHERE tr.assignment.user.id = :userId"),
        @NamedQuery(name = "TimeReportDayPO.countByProject", query = "SELECT count(tr) FROM TimeReportDay tr WHERE tr.assignment.project.id = :projectId"),
        @NamedQuery(name = "TimeReportDayPO.findByProjectAndBetweenDates", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.project.id = :idProject AND date >= :startDate and date <= :endDate ORDER BY tr.date"),
        @NamedQuery(name = "TimeReportDayPO.sumReportedTimeByUser", query = "SELECT sum(tr.time) FROM TimeReportDay tr WHERE tr.assignment.user.id = :idUser"),
        @NamedQuery(name = "TimeReportDayPO.findByUser", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.user.id = :idUser"),
        @NamedQuery(name = "TimeReportDayPO.sumReportedTimeByProject", query = "SELECT sum(tr.time) FROM TimeReportDay tr WHERE tr.assignment.project.id = :idProject"),
        @NamedQuery(name = "TimeReportDayPO.sumReportedTimeByCompany", query = "SELECT sum(tr.time) FROM TimeReportDay tr WHERE tr.assignment.project.company.id = :idCompany"),
        @NamedQuery(name = "TimeReportDayPO.findByProject", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.project.id = :idProject"),
        @NamedQuery(name = "TimeReportDayPO.findByCompany", query = "SELECT tr FROM TimeReportDay tr WHERE tr.assignment.project.company.id = :idCompany"),
})
public class TimeReportDayPO extends BasePO {
    private long id;
    private AssignmentPO assignment;
    private LocalDate date;
    private float time;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_timereport")
    @SequenceGenerator(name = "seq_timereport", sequenceName = "seq_timereport", allocationSize = 1)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assignment", nullable = false)
    public AssignmentPO getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentPO assignment) {
        this.assignment = assignment;
    }

    @Column(name = "date", unique = false, nullable = false, updatable = false)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Column(name = "reportedtime", unique = false, nullable = false, updatable = true)
    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
