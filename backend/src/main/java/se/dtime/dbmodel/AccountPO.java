package se.dtime.dbmodel;

import jakarta.persistence.*;
import se.dtime.model.ActivationStatus;

@Entity(name = "Account")
@Table(name = "account")
@NamedQueries({
        @NamedQuery(name = "Account.findByActivationStatusOrderByFirstNameAsc", query = "SELECT o FROM Account o WHERE o.activationStatus=:acticationStatus ORDER BY o.name DESC")
})
public class AccountPO extends BasePO {
    private Long id;
    private String name;
    private ActivationStatus activationStatus;

    public AccountPO() {

    }

    public AccountPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_account")
    @SequenceGenerator(name = "seq_account", sequenceName = "seq_account", allocationSize = 1)
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
