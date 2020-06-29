package se.dtime.dbmodel;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name="PublicHoliday")
@Table(name = "publicholiday")
public class PublicHolidayPO extends BasePO {
    private Long id;
    private String name;
    private boolean workday;

    public PublicHolidayPO() {

    }

    public PublicHolidayPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "name", unique = true, nullable = false, length = 50)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Type(type="numeric_boolean")
    @Column(name = "workday", unique = false, nullable = false)
    public boolean isWorkday() {
        return workday;
    }

    public void setWorkday(boolean workday) {
        this.workday = workday;
    }
}
