package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.ActivityPO;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityPO, Long> {
    List<ActivityPO> findByVoters_Id(long id);
}
