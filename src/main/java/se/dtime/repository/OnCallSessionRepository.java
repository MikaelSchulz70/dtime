package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.oncall.OnCallSessionPO;

@Repository
public interface OnCallSessionRepository extends JpaRepository<OnCallSessionPO, Long> {

}
