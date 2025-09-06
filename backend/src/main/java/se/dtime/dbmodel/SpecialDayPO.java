package se.dtime.dbmodel;

import jakarta.persistence.*;
import se.dtime.model.timereport.DayType;

import java.time.LocalDate;

@Entity(name = "SpecialDay")
@Table(name = "special_day")
public class SpecialDayPO extends BasePO {
    private Long id;
    private String name;
    private DayType dayType;
    private LocalDate date;

    public SpecialDayPO() {

    }

    public SpecialDayPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_special_ay")
    @SequenceGenerator(name = "seq_special_day", sequenceName = "seq_special_day", allocationSize = 1)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = false, nullable = false, length = 40)
    public DayType getDayType() {
        return dayType;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    @Column(name = "date", unique = false, nullable = false)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
