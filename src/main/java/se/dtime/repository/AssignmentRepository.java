package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AssignmentPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentPO, Long> {
    List<AssignmentPO> findByUser(@Param("user") UserPO user);

    List<AssignmentPO> findByProject(@Param("project") ProjectPO project);

    List<AssignmentPO> findByActivationStatus(@Param("activationStatus") ActivationStatus activationStatus);

    List<AssignmentPO> findByUserAndActivationStatus(@Param("user") UserPO user, @Param("activationStatus") ActivationStatus activationStatus);

    List<AssignmentPO> findByProjectAndActivationStatus(@Param("project") ProjectPO project, @Param("activationStatus") ActivationStatus activationStatus);

    List<AssignmentPO> findByProjectOnCallTrueAndActivationStatus(@Param("activationStatus") ActivationStatus activationStatus);

    AssignmentPO findByUserAndProject(@Param("user") UserPO user, @Param("project") ProjectPO project);

    long countByUser(@Param("user") UserPO user);

    long countByProject(@Param("project") ProjectPO project);

    long countByUserAndActivationStatusAndProjectOnCallTrue(@Param("user") UserPO user, @Param("activationStatus") ActivationStatus activationStatus);
}
