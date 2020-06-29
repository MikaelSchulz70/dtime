package se.dtime.dbmodel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "Rate")
@Table(name = "rate")
@NamedQueries({
        @NamedQuery(name = "RatePO.findCurrentRates", query = "SELECT r FROM Rate r WHERE r.fromDate is not null AND r.toDate is null")
})
public class RatePO extends BasePO {
    private Long id;
    private AssignmentPO assignment;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal customerRate;
    private BigDecimal subcontractorRate;
    private String comment;

    public RatePO() {
    }

    public RatePO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_rate")
    @SequenceGenerator(name = "seq_rate", sequenceName = "seq_rate", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assignment", nullable = false, updatable = false)
    public AssignmentPO getAssignment() {
        return assignment;
    }

    public void setAssignment(AssignmentPO assignment) {
        this.assignment = assignment;
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

    @Column(name = "ratesubcontractor", unique = false, nullable = true, updatable = true)
    public BigDecimal getSubcontractorRate() {
        return subcontractorRate;
    }

    public void setSubcontractorRate(BigDecimal subcontractorRate) {
        this.subcontractorRate = subcontractorRate;
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
        RatePO ratePO = (RatePO) o;
        return Objects.equals(getId(), ratePO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
