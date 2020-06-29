package se.dtime.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {
   private SystemPropertyDB[] systemProperties;
   private PublicHoliday[] publicHolidays;
}
