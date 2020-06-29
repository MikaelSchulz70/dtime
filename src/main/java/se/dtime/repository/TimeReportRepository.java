package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.timereport.TimeReportDayPO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeReportRepository extends JpaRepository<TimeReportDayPO, Long> {
    TimeReportDayPO findByAssignmentAndDate(@Param("idAssignment") long idAssignment, @Param("date") LocalDate date);

    List<TimeReportDayPO> findByUserAndDate(@Param("idUser") long idUser, @Param("date") LocalDate date);

    List<TimeReportDayPO> findByUserAndBetweenDates(@Param("idUser") long idUser, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    long countByUserId(@Param("userId") long userId);

    long countByProject(@Param("projectId") long projectId);

    void deleteById(Long id);

    List<TimeReportDayPO> findByProjectAndBetweenDates(@Param("idProject") long idProject, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    BigDecimal sumReportedTimeByUser(@Param("idUser") long idUser);

    BigDecimal sumReportedTimeByProject(@Param("idProject") long idProject);

    BigDecimal sumReportedTimeByCompany(@Param("idCompany") long idCompany);

    List<TimeReportDayPO> findByUser(@Param("idUser") long idUser);

    List<TimeReportDayPO> findByProject(@Param("idProject") long idProject);

    List<TimeReportDayPO> findByCompany(@Param("idCompany") long idCompany);
}
