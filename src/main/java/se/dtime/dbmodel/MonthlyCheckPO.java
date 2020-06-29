package se.dtime.dbmodel;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "MonthlyCheck")
@Table(name = "monthlycheck")
public class MonthlyCheckPO extends BasePO {
    private Long id;
    private CompanyPO company;
    private LocalDate date;
    private boolean invoiceVerified;
    private boolean invoiceSent;

    public MonthlyCheckPO() {
    }

    public MonthlyCheckPO(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_monthlycheck")
    @SequenceGenerator(name = "seq_monthlycheck", sequenceName = "seq_monthlycheck", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_company", nullable = false, updatable = false)
    public CompanyPO getCompany() {
        return company;
    }

    public void setCompany(CompanyPO company) {
        this.company = company;
    }

    @Column(name = "date", unique = false, nullable = false, updatable = true)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "invoiceverified", unique = false, nullable = false)
    public boolean isInvoiceVerified() {
        return invoiceVerified;
    }

    public void setInvoiceVerified(boolean invoiceVerified) {
        this.invoiceVerified = invoiceVerified;
    }

    @Type(type = "numeric_boolean")
    @Column(name = "invoicesent", unique = false, nullable = false)
    public boolean isInvoiceSent() {
        return invoiceSent;
    }

    public void setInvoiceSent(boolean invoiceSent) {
        this.invoiceSent = invoiceSent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyCheckPO ratePO = (MonthlyCheckPO) o;
        return Objects.equals(getId(), ratePO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
