package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import se.dtime.dbmodel.UserPO;
import se.dtime.dbmodel.timereport.CloseDatePO;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CloseDateRepository extends JpaRepository<CloseDatePO, Long> {
    @Transactional
    void deleteByUserAndDate(@Param("user") UserPO userPO, @Param("date") LocalDate date);

    CloseDatePO findByUserAndDate(@Param("user") UserPO userPO, @Param("date") LocalDate date);

    List<CloseDatePO> findByUser(@Param("user") UserPO userPO);
}
