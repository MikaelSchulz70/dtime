package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.oncall.OnCallConfigPO;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.model.ActivationStatus;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface OnCallConfigRepository extends JpaRepository<OnCallConfigPO, Long> {
    OnCallConfigPO findByProjectIdAndDayOfWeek(long idProject, DayOfWeek dayOfWeek);
    List<OnCallConfigPO> findByProject(ProjectPO projectPO);
    List<OnCallConfigPO> findByDayOfWeekAndActivationStatus(DayOfWeek dayOfWeek, ActivationStatus activationStatus);
}
