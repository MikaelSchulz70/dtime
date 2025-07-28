package se.dtime.model.report;

import java.math.BigDecimal;

public class FollowUpBaseReport {
    private BigDecimal totalHours = BigDecimal.ZERO;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal amountSubcontractor = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String comment;

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountSubcontractor() {
        return amountSubcontractor;
    }

    public void setAmountSubcontractor(BigDecimal amountSubcontractor) {
        this.amountSubcontractor = amountSubcontractor;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
