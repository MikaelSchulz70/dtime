package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.PublicHolidayPO;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHolidayPO, Long> {

}
