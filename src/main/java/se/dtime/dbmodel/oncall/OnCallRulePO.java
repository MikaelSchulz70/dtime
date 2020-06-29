package se.dtime.dbmodel.oncall;

import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.ProjectPO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity(name="OnCallRule")
@Table(name = "oncallrule")
public class OnCallRulePO extends BasePO {
    private Long id;
    private ProjectPO project;
    private String fromEmail;
    private String subjectCSV;
    private String bodyCSV;

    public OnCallRulePO() {

    }

    public OnCallRulePO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq_oncallrule")
    @SequenceGenerator(name="seq_oncallrule", sequenceName="seq_oncallrule", allocationSize=1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_project", nullable = false, updatable = false)
    public ProjectPO getProject() {
        return project;
    }

    public void setProject(ProjectPO project) {
        this.project = project;
    }

    @Column(name = "fromemail", unique = true, nullable = false, length=60)
    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    @Column(name = "subjectcsv", unique = false, nullable = true, length=100)
    public String getSubjectCSV() {
        return subjectCSV;
    }

    public void setSubjectCSV(String subjectCSV) {
        this.subjectCSV = subjectCSV;
    }

    @Column(name = "bodycsv", unique = false, nullable = true, length=100)
    public String getBodyCSV() {
        return bodyCSV;
    }

    public void setBodyCSV(String bodyCSV) {
        this.bodyCSV = bodyCSV;
    }

    @Transient
    public List<String> getSubjectCSVAsArray() {
        if (subjectCSV == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(subjectCSV.split(",")).map(String::trim).collect(Collectors.toList());
    }

    @Transient
    public List<String> getBodyCSVAsArray() {
        if (bodyCSV == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(bodyCSV.split(",")).map(String::trim).collect(Collectors.toList());

    }
}
