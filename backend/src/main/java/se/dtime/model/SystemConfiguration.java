package se.dtime.model;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
   private SystemPropertyDB[] systemProperties;
   private PublicHoliday[] publicHolidays;

   public SystemPropertyDB[] getSystemProperties() {
       return systemProperties;
   }

   public void setSystemProperties(SystemPropertyDB[] systemProperties) {
       this.systemProperties = systemProperties;
   }

   public PublicHoliday[] getPublicHolidays() {
       return publicHolidays;
   }

   public void setPublicHolidays(PublicHoliday[] publicHolidays) {
       this.publicHolidays = publicHolidays;
   }
}
