package se.dtime.dbmodel.timereport;

import se.dtime.dbmodel.BasePO;
import se.dtime.dbmodel.UserPO;

import javax.persistence.*;
import java.time.LocalDate;

@Entity(name = "CloseDate")
@Table(name = "closedate")
public class CloseDatePO extends BasePO {
    private Long id;
    private UserPO user;
    private LocalDate date;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_closedate")
    @SequenceGenerator(name = "seq_closedate", sequenceName = "seq_closedate", allocationSize = 1)
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

    @Column(name = "date", unique = false, nullable = false, updatable = false)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
