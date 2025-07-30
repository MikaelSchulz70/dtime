package se.dtime.dbmodel.timereport;

import jakarta.persistence.*;
import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.TaskContributorPO;

import java.time.LocalDate;

@Entity(name = "TimeEntry")
@Table(name = "time_report")
@NamedQueries({
        @NamedQuery(name = "TimeEntryPO.findByUserAndDate", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.user.id = :userId AND date=:date ORDER BY te.date"),
        @NamedQuery(name = "TimeEntryPO.findByUserAndBetweenDates", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.user.id = :userId AND date >= :startDate and date <= :endDate ORDER BY te.date"),
        @NamedQuery(name = "TimeEntryPO.findByParticipationAndDate", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.id = :taskIdContributor AND date =:date"),
        @NamedQuery(name = "TimeEntryPO.countByUserId", query = "SELECT count(te) FROM TimeEntry te WHERE te.taskContributor.user.id = :userId"),
        @NamedQuery(name = "TimeEntryPO.countByTask", query = "SELECT count(te) FROM TimeEntry te WHERE te.taskContributor.task.id = :taskId"),
        @NamedQuery(name = "TimeEntryPO.findByTaskAndBetweenDates", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.task.id = :taskId AND date >= :startDate and date <= :endDate ORDER BY te.date"),
        @NamedQuery(name = "TimeEntryPO.sumReportedTimeByUser", query = "SELECT sum(te.time) FROM TimeEntry te WHERE te.taskContributor.user.id = :userId"),
        @NamedQuery(name = "TimeEntryPO.findByUser", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.user.id = :userId"),
        @NamedQuery(name = "TimeEntryPO.sumReportedTimeByTask", query = "SELECT sum(te.time) FROM TimeEntry te WHERE te.taskContributor.task.id = :taskId"),
        @NamedQuery(name = "TimeEntryPO.sumReportedTimeByAccount", query = "SELECT sum(te.time) FROM TimeEntry te WHERE te.taskContributor.task.account.id = :accountId"),
        @NamedQuery(name = "TimeEntryPO.findByTask", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.task.id = :taskId"),
        @NamedQuery(name = "TimeEntryPO.findByAccount", query = "SELECT te FROM TimeEntry te WHERE te.taskContributor.task.account.id = :accountId"),
})
public class TimeEntryPO extends BasePO {
    private long id;
    private TaskContributorPO taskContributor;
    private LocalDate date;
    private float time;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_timeentry")
    @SequenceGenerator(name = "seq_timeentry", sequenceName = "seq_timeentry", allocationSize = 1)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task_contributor", nullable = false)
    public TaskContributorPO getTaskContributor() {
        return taskContributor;
    }

    public void setTaskContributor(TaskContributorPO taskContributor) {
        this.taskContributor = taskContributor;
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
