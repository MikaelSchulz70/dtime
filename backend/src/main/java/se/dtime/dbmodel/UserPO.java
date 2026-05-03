package se.dtime.dbmodel;

import jakarta.persistence.*;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

@Entity(name = "User")
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findByActivationStatusOrderByDisplayNameAsc", query = "SELECT u FROM User u WHERE u.activationStatus=:acticationStatus ORDER BY u.displayName")
})
public class UserPO extends BasePO {
    private Long id;
    private String externalId;
    private String displayName;
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


    @Column(name = "external_id", unique = true, nullable = false, updatable = true, length = 120)
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Column(name = "displayname", unique = false, nullable = false, length = 80)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = normalizeDisplayName(displayName);
    }

    @Transient
    public String getFirstName() {
        if (displayName == null || displayName.isBlank()) {
            return "";
        }
        String[] parts = displayName.trim().split("\\s+", 2);
        return parts[0];
    }

    public void setFirstName(String firstName) {
        setDisplayName(joinNameParts(firstName, getLastName()));
    }

    @Transient
    public String getLastName() {
        if (displayName == null || displayName.isBlank()) {
            return "";
        }
        String[] parts = displayName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "-";
    }

    public void setLastName(String lastName) {
        setDisplayName(joinNameParts(getFirstName(), lastName));
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
        return displayName;
    }

    private String normalizeDisplayName(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return "-";
        }
        return candidate.trim().replaceAll("\\s+", " ");
    }

    private String joinNameParts(String firstName, String lastName) {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        if (last.isBlank()) {
            last = "-";
        }
        String combined = (first + " " + last).trim();
        return combined.isBlank() ? "-" : combined;
    }
}
