package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.oncall.OnCallPO;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OnCallRepository extends JpaRepository<OnCallPO, Long> {
    List<OnCallPO> findByBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    OnCallPO findByAssignmentAndDate(@Param("idAssignment") long idAssignment, @Param("date") LocalDate date);
    List<OnCallPO> findByProject(@Param("idProject") long idProject);
    List<OnCallPO> findByProjectAndDate(@Param("idProject") long idProject, @Param("date") LocalDate date);
}
