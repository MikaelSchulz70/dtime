package se.dtime.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserPO, Long> {


    UserPO findByEmail(String email);

    List<UserPO> findByActivationStatusOrderByFirstNameAsc(ActivationStatus activationStatus);

    List<UserPO> findByUserRoleAndActivationStatus(UserRole userRole, ActivationStatus activationStatus);

    Page<UserPO> findByActivationStatus(Pageable pageable, ActivationStatus activationStatus);
    
    Page<UserPO> findAll(Pageable pageable);
}
