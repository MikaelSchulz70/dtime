package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AccountPO;
import se.dtime.model.ActivationStatus;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountPO, Long> {
    AccountPO findByName(String name);

    List<AccountPO> findByActivationStatusOrderByNameAsc(ActivationStatus activationStatus);
}
