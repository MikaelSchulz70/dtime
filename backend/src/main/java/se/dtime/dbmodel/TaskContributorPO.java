package se.dtime.dbmodel;

import jakarta.persistence.*;
import se.dtime.model.ActivationStatus;

@Entity(name = "TaskContributor")
@Table(name = "task_contributor")
@NamedQueries({
        @NamedQuery(name = "TaskContributor.findByTask", query = "SELECT p FROM TaskContributor p WHERE p.task.id = :task"),
        @NamedQuery(name = "TaskContributor.findByUser", query = "SELECT p FROM TaskContributor p WHERE p.user.id = :user"),
        @NamedQuery(name = "TaskContributor.countByUserId", query = "SELECT count(p) FROM TaskContributor p WHERE p.user.id = :user"),
        @NamedQuery(name = "TaskContributor.countByTask", query = "SELECT count(p) FROM TaskContributor p WHERE p.task.id = :task")
})
public class TaskContributorPO extends BasePO {
    private Long id;
    private UserPO user;
    private TaskPO task;
    private ActivationStatus activationStatus;

    public TaskContributorPO() {

    }

    public TaskContributorPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_participation")
    @SequenceGenerator(name = "seq_participation", sequenceName = "seq_participation", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false, updatable = false)
    public UserPO getUser() {
        return user;
    }

    public void setUser(UserPO user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_task", nullable = false, updatable = false)
    public TaskPO getTask() {
        return task;
    }

    public void setTask(TaskPO task) {
        this.task = task;
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
