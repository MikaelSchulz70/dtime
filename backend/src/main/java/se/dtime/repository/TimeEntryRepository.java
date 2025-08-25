package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.timereport.TimeEntryPO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntryPO, Long> {
    @Query("SELECT te FROM TimeEntry te WHERE te.taskContributor.id = :taskIdContributor AND te.date = :date")
    TimeEntryPO findByTaskContributorAndDate(@Param("taskIdContributor") long taskIdContributor, @Param("date") LocalDate date);

    List<TimeEntryPO> findByUserAndDate(@Param("userId") long userId, @Param("date") LocalDate date);

    List<TimeEntryPO> findByUserAndBetweenDates(@Param("userId") long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByUserId(@Param("userId") long userId);

    long countByTask(@Param("taskId") long taskId);

    void deleteById(Long id);

    List<TimeEntryPO> findByTaskAndBetweenDates(@Param("taskId") long taskId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    BigDecimal sumReportedTimeByUser(@Param("userId") long userId);

    BigDecimal sumReportedTimeByTask(@Param("taskId") long taskId);

    BigDecimal sumReportedTimeByAccount(@Param("accountId") long accountId);

    List<TimeEntryPO> findByUser(@Param("userId") long userId);

    List<TimeEntryPO> findByTask(@Param("taskId") long taskId);

    List<TimeEntryPO> findByAccount(@Param("accountId") long accountId);

    @Query("SELECT te FROM TimeEntry te WHERE te.date >= :startDate AND te.date <= :endDate ORDER BY te.date")
    List<TimeEntryPO> findByBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
