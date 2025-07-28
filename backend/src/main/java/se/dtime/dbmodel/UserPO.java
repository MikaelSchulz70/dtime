package se.dtime.dbmodel;

import jakarta.persistence.*;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

@Entity(name = "User")
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findByActivationStatusOrderByFirstNameAsc", query = "SELECT u FROM User u WHERE u.activationStatus=:acticationStatus ORDER BY u.firstName, u.lastName")
})
public class UserPO extends BasePO {
    private Long id;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole userRole;
    private ActivationStatus activationStatus;

    public UserPO() {
    }

    public UserPO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_users")
    @SequenceGenerator(name = "seq_users", sequenceName = "seq_users", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    @Column(name = "password", unique = false, nullable = false, updatable = true, length = 80)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "firstname", unique = false, nullable = false, length = 30)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Column(name = "lastname", unique = false, nullable = false, length = 30)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Column(name = "email", unique = true, nullable = false, length = 60)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "userrole", unique = false, nullable = false, length = 20)
    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", unique = false, nullable = false, length = 20)
    public ActivationStatus getActivationStatus() {
        return activationStatus;
    }

    public void setActivationStatus(ActivationStatus activationStatus) {
        this.activationStatus = activationStatus;
    }

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
