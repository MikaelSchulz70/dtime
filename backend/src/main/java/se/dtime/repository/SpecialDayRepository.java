package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.SpecialDayPO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDayPO, Long> {

    @Query("SELECT s FROM SpecialDay s WHERE YEAR(s.date) = :year ORDER BY s.date")
    List<SpecialDayPO> findByYear(@Param("year") int year);

    @Query("SELECT DISTINCT YEAR(s.date) FROM SpecialDay s ORDER BY YEAR(s.date) DESC")
    List<Integer> findDistinctYears();

    Optional<SpecialDayPO> findByDate(LocalDate date);

    boolean existsByDate(LocalDate date);

}
