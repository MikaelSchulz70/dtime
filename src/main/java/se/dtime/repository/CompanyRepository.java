package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.CompanyPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyPO, Long> {
    CompanyPO findByName(String name);
    List<CompanyPO> findByActivationStatusOrderByNameAsc(ActivationStatus activationStatus);
}
