package se.dtime.dbmodel;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BasePO {
    private long createdBy;
    private long updatedBy;
    private LocalDateTime createDateTime;
    private LocalDateTime updatedDateTime;

    @Column(name = "createdby", unique = false, nullable = false, updatable=false)
    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "updatedby", unique = false, nullable = false, updatable=true)
    public long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(long updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Column(name = "createdatetime", unique = false, nullable = false, updatable=false)
    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    @Column(name = "updatedatetime", unique = false, nullable = false, updatable=true)
    public LocalDateTime getUpdatedDateTime() {
        return updatedDateTime;
    }

    public void setUpdatedDateTime(LocalDateTime updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }
}
