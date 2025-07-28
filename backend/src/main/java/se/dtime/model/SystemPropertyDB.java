package se.dtime.model;

import lombok.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPropertyDB {
    private Long id;
    @NotNull
    @Size(min = 1, max = 80, message = "Name max 50 characters")
    private String name;
    @Size(min = 0, max = 100, message = "Value max 100 characters")
    private String value;
    private SystemPropertyType systemPropertyType;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SystemPropertyType getSystemPropertyType() {
        return systemPropertyType;
    }

    public void setSystemPropertyType(SystemPropertyType systemPropertyType) {
        this.systemPropertyType = systemPropertyType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
