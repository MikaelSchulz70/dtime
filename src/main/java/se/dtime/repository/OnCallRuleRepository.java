package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.ProjectPO;
import se.dtime.dbmodel.oncall.OnCallRulePO;

import java.util.List;

@Repository
public interface OnCallRuleRepository extends JpaRepository<OnCallRulePO, Long> {
    List<OnCallRulePO> findByProject(ProjectPO projectPO);
    OnCallRulePO findByFromEmail(String fromMail);
}
