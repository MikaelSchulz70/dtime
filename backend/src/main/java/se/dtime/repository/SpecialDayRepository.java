package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.SpecialDayPO;

import java.util.List;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDayPO, Long> {

    @Query("SELECT s FROM SpecialDay s WHERE YEAR(s.date) = :year")
    List<SpecialDayPO> findByYear(@Param("year") int year);

}
