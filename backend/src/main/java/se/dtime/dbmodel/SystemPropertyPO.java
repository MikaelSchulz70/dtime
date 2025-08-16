package se.dtime.dbmodel;

import se.dtime.model.SystemPropertyType;

import jakarta.persistence.*;

@Entity(name="SystemPropertyDB")
@Table(name = "systemproperty")
public class SystemPropertyPO extends BasePO {
    private Long id;
    private String name;
    private String value;
    private SystemPropertyType systemPropertyType;
    private String description;

    public SystemPropertyPO() {

    }

    public SystemPropertyPO(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_systemproperty")
    @SequenceGenerator(name = "seq_systemproperty", sequenceName = "seq_systemproperty", allocationSize = 1)
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

    @Column(name = "value", unique = true, nullable = true, length = 100)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "type", unique = false, nullable = true, insertable = false , updatable = false, length = 20)
    public SystemPropertyType getSystemPropertyType() {
        return systemPropertyType;
    }

    public void setSystemPropertyType(SystemPropertyType systemPropertyType) {
        this.systemPropertyType = systemPropertyType;
    }

    @Column(name = "description", unique = false, nullable = true, length = 50)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
