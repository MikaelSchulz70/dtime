package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectPO, Long> {
    List<ProjectPO> findByName(String name);
    List<ProjectPO> findByActivationStatus(@Param("activationStatus") ActivationStatus activationStatus);
    List<ProjectPO> findByCompany(@Param("company") CompanyPO company);
    List<ProjectPO> findByOnCallTrueAndActivationStatus(@Param("activationStatus") ActivationStatus activationStatus);
    List<ProjectPO> findByOnCallTrue();
}
