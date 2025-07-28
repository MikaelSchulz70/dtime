package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.MonthlyCheckPO;

import java.time.LocalDate;

@Repository
public interface MonthlyCheckRepository extends JpaRepository<MonthlyCheckPO, Long> {
    MonthlyCheckPO findByAccountAndDate(AccountPO accountPO, LocalDate date);
}
