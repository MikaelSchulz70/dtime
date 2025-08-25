package se.dtime.dbmodel;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import se.dtime.model.ActivationStatus;
import se.dtime.model.TaskType;

import java.util.Objects;

@Entity(name = "Task")
@Table(name = "task")
@NamedQueries({
        @NamedQuery(name = "Task.findByActivationStatusOrderByFirstNameAsc", query = "SELECT c FROM Task c WHERE c.activationStatus = :activationStatus"),
        @NamedQuery(name = "Task.findByAccount", query = "SELECT c FROM Task c WHERE c.account.id = :accountId")
})
public class TaskPO extends BasePO {
    private Long id;
    private String name;
    private ActivationStatus activationStatus;
    private TaskType taskType;
    private AccountPO account;

    public TaskPO() {
    }

    public TaskPO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_task")
    @SequenceGenerator(name = "seq_task", sequenceName = "seq_task", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", unique = true, nullable = false, length = 80)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false, length = 20)
    public ActivationStatus getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(ActivationStatus activationStatus) {
        this.activationStatus = activationStatus;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", unique = false, nullable = false, length = 20)
    public TaskType getTaskType() {
        return taskType != null ? taskType : TaskType.NORMAL;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_account", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public AccountPO getAccount() {
        return account;
    }

    public void setAccount(AccountPO account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskPO taskPO = (TaskPO) o;
        return Objects.equals(getId(), taskPO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
