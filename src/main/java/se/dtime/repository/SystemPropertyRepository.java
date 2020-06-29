package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.SystemPropertyPO;

@Repository
public interface SystemPropertyRepository extends JpaRepository<SystemPropertyPO, Long> {
    SystemPropertyPO findByName(String name);
}
