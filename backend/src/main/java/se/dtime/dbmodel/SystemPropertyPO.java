package se.dtime.dbmodel;

import jakarta.persistence.*;
import lombok.Setter;
import se.dtime.model.SystemPropertyType;

@Setter
@Entity(name = "SystemPropertyDB")
@Table(name = "\"systemproperty\"")
public class SystemPropertyPO extends BasePO {
    private Long id;
    private String name;
    private String value;
    private SystemPropertyType systemPropertyType;
    private String description;

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_systemproperty")
    @SequenceGenerator(name = "seq_systemproperty", sequenceName = "seq_systemproperty", allocationSize = 1)
    public Long getId() {
        return id;
    }

    @Column(name = "name", unique = true, nullable = false, length = 50)
    public String getName() {
        return name;
    }

    @Column(name = "\"value\"", unique = true, nullable = true, length = 100)
    public String getValue() {
        return value;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "\"type\"", unique = false, nullable = true, insertable = false, updatable = false, length = 20)
    public SystemPropertyType getSystemPropertyType() {
        return systemPropertyType;
    }

    @Column(name = "description", unique = false, nullable = true, length = 50)
    public String getDescription() {
        return description;
    }

}
