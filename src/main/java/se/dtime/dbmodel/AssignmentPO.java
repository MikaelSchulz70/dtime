package se.dtime.dbmodel;

import se.dtime.model.ActivationStatus;

import javax.persistence.*;

@Entity(name = "Assignment")
@Table(name = "assignment")
@NamedQueries({
        @NamedQuery(name = "Assignment.findByProject", query = "SELECT a FROM Assignment a WHERE a.project.id = :project"),
        @NamedQuery(name = "Assignment.findByUser", query = "SELECT a FROM Assignment a WHERE a.user.id = :user"),
        @NamedQuery(name = "Assignment.countByUserId", query = "SELECT count(a) FROM Assignment a WHERE a.user.id = :user"),
        @NamedQuery(name = "Assignment.countByProject", query = "SELECT count(a) FROM Assignment a WHERE a.project.id = :project"),
        @NamedQuery(name = "Assignment.countOnCallProjects", query = "SELECT count(a) FROM Assignment a WHERE a.user.id = :idUser and a.project.onCall = true")
})
public class AssignmentPO extends BasePO {
    private Long id;
    private UserPO user;
    private ProjectPO project;
    private ActivationStatus activationStatus;

    public AssignmentPO() {

    }

    public AssignmentPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_assignment")
    @SequenceGenerator(name = "seq_assignment", sequenceName = "seq_assignment", allocationSize = 1)
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
    @JoinColumn(name = "id_project", nullable = false, updatable = false)
    public ProjectPO getProject() {
        return project;
    }

    public void setProject(ProjectPO project) {
        this.project = project;
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
