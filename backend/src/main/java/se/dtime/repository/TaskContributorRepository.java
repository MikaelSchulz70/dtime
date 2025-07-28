package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface TaskContributorRepository extends JpaRepository<TaskContributorPO, Long> {
    List<TaskContributorPO> findByUser(@Param("user") UserPO user);

    List<TaskContributorPO> findByTask(@Param("task") TaskPO task);
    
    List<TaskContributorPO> findByUserAndActivationStatus(@Param("user") UserPO user, @Param("activationStatus") ActivationStatus activationStatus);

    TaskContributorPO findByUserAndTask(@Param("user") UserPO user, @Param("task") TaskPO task);

    long countByUser(@Param("user") UserPO user);
}
