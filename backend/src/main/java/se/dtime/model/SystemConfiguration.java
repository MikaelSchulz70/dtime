package se.dtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
    private SystemPropertyDB[] systemProperties;
    private SpecialDay[] specialDays;

    public SystemPropertyDB[] getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(SystemPropertyDB[] systemProperties) {
        this.systemProperties = systemProperties;
    }

    public SpecialDay[] getSpecialDays() {
        return specialDays;
    }

    public void setSpecialDays(SpecialDay[] specialDays) {
        this.specialDays = specialDays;
    }
}
