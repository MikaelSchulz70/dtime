package se.dtime.dbmodel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "FixRate")
@Table(name = "fixrate")
@NamedQueries({
        @NamedQuery(name = "FixRatePO.findCurrentFixRates", query = "SELECT r FROM FixRate r WHERE r.fromDate is not null AND r.toDate is null")
})
public class FixRatePO extends BasePO {
    private Long id;
    private ProjectPO project;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal customerRate;
    private String comment;

    public FixRatePO() {
    }

    public FixRatePO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_fixrate")
    @SequenceGenerator(name = "seq_fixrate", sequenceName = "seq_fixrate", allocationSize = 1)
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

    @Column(name = "fromdate", unique = false, nullable = false, updatable = true)
    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    @Column(name = "todate", unique = false, nullable = true, updatable = true)
    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Column(name = "ratecustomer", unique = false, nullable = true, updatable = true)
    public BigDecimal getCustomerRate() {
        return customerRate;
    }

    public void setCustomerRate(BigDecimal customerRate) {
        this.customerRate = customerRate;
    }

    @Column(name = "comment", unique = false, nullable = false, length = 250)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixRatePO ratePO = (FixRatePO) o;
        return Objects.equals(getId(), ratePO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
