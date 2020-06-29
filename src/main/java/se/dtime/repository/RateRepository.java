package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.RatePO;

import java.util.List;

@Repository
public interface RateRepository extends JpaRepository<RatePO, Long> {
    List<RatePO> findCurrentRates();

    List<RatePO> findByAssignmentOrderByFromDateDesc(AssignmentPO assignment);

    @Query("select r from Rate r where r.assignment.project.id = :idProject")
    List<RatePO> findByProject(long idProject);
}
