package se.dtime.dbmodel;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import se.dtime.model.ActivationStatus;
import se.dtime.model.ProjectCategory;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "Project")
@Table(name = "project")
@NamedQueries({
        @NamedQuery(name = "Project.findByActivationStatusOrderByFirstNameAsc", query = "SELECT p FROM Project p WHERE p.activationStatus = :activationStatus"),
        @NamedQuery(name = "Project.findByCompany", query = "SELECT p FROM Project p WHERE p.company.id = :company")
})
public class ProjectPO extends BasePO {
    private Long id;
    private String name;
    private ActivationStatus activationStatus;
    private CompanyPO company;
    private boolean provision;
    private boolean internal;
    private boolean onCall;
    private boolean fixRate;
    private ProjectCategory projectCategory;

    public ProjectPO() {
    }

    public ProjectPO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_project")
    @SequenceGenerator(name = "seq_project", sequenceName = "seq_project", allocationSize = 1)
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

    @Type(type = "numeric_boolean")
    @Column(name = "provision", unique = false, nullable = false)
    public boolean isProvision() {
        return provision;
    }

    public void setProvision(boolean provision) {
        this.provision = provision;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "internal", unique = false, nullable = false)
    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "oncall", unique = false, nullable = false)
    public boolean isOnCall() {
        return onCall;
    }

    public void setOnCall(boolean onCall) {
        this.onCall = onCall;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_company", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public CompanyPO getCompany() {
        return company;
    }

    public void setCompany(CompanyPO company) {
        this.company = company;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "category", unique = false, nullable = false, length = 20)
    public ProjectCategory getProjectCategory() {
        return projectCategory;
    }

    public void setProjectCategory(ProjectCategory projectCategory) {
        this.projectCategory = projectCategory;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "fixrate", unique = false, nullable = false)
    public boolean isFixRate() {
        return fixRate;
    }

    public void setFixRate(boolean fixRate) {
        this.fixRate = fixRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectPO projectPO = (ProjectPO) o;
        return Objects.equals(getId(), projectPO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
