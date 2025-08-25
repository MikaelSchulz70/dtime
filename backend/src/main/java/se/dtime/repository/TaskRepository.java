package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.TaskType;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<TaskPO, Long> {
    List<TaskPO> findByName(String name);

    List<TaskPO> findByActivationStatus(@Param("activationStatus") ActivationStatus activationStatus);

    List<TaskPO> findByAccount(@Param("account") AccountPO account);

    List<TaskPO> findByTaskTypeAndAccount(@Param("taskType") TaskType taskType, @Param("account") AccountPO account);
}
