package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.oncall.OnCallAlarmPO;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OnCallAlarmRepository extends JpaRepository<OnCallAlarmPO, Long> {
    List<OnCallAlarmPO> findAllByOrderByCreateDateTimeDesc();

    List<OnCallAlarmPO> findByUserOrderByCreateDateTimeDesc(@Param("user") UserPO user);

    OnCallAlarmPO findByIdAndUser(@Param("id") long id, @Param("user") UserPO user);

    @Modifying
    @Transactional
    @Query("DELETE FROM OnCallAlarm a WHERE a.createDateTime <= :date")
    void deleteByCreateDateTimeBefore(@Param("date") LocalDateTime date);

    List<OnCallAlarmPO> findByProject(ProjectPO projectPO);
}
