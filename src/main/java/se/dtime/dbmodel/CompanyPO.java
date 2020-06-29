package se.dtime.dbmodel;

import se.dtime.model.ActivationStatus;

import javax.persistence.*;

@Entity(name="Company")
@Table(name = "company")
@NamedQueries({
        @NamedQuery(name = "Company.findByActivationStatusOrderByFirstNameAsc", query = "SELECT c FROM Company c WHERE c.activationStatus=:acticationStatus ORDER BY c.name DESC")
})
public class CompanyPO extends BasePO {
    private Long id;
    private String name;
    private ActivationStatus activationStatus;

    public CompanyPO() {

    }

    public CompanyPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seq_company")
    @SequenceGenerator(name="seq_company", sequenceName="seq_company", allocationSize=1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", unique = true, nullable = false, length = 40)
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
}
