package se.dtime.dbmodel;

import javax.persistence.*;
import java.util.List;

@Entity(name = "Activity")
@Table(name = "activity")
public class ActivityPO extends BasePO {
    private Long id;
    private String description;
    private List<UserPO> voters;

    public ActivityPO() {
    }

    public ActivityPO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_activity")
    @SequenceGenerator(name = "seq_activity", sequenceName = "seq_activity", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "description", unique = true, nullable = false, length = 100)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinTable(name = "activityuser",
            joinColumns = {@JoinColumn(name = "id_activity", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "id_user", referencedColumnName = "id", unique = true)})
    public List<UserPO> getVoters() {
        return voters;
    }

    public void setVoters(List<UserPO> voters) {
        this.voters = voters;
    }
}
