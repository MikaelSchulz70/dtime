package se.dtime.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountPO, Long> {
    AccountPO findByName(String name);

    List<AccountPO> findByActivationStatusOrderByNameAsc(ActivationStatus activationStatus);

    Page<AccountPO> findByActivationStatus(Pageable pageable, ActivationStatus activationStatus);
    
    Page<AccountPO> findAll(Pageable pageable);
}
