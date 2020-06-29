package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.FixRatePO;
import se.dtime.dbmodel.ProjectPO;

import java.util.List;

@Repository
public interface FixRateRepository extends JpaRepository<FixRatePO, Long> {
    List<FixRatePO> findCurrentFixRates();

    List<FixRatePO> findByProjectOrderByFromDateDesc(ProjectPO project);

    List<FixRatePO> findByProject(ProjectPO project);
}
