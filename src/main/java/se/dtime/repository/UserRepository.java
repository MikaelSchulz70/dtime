package se.dtime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.UserRole;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserPO, Long> {

    UserPO findByUserName(String userName);
    UserPO findByEmail(String email);
    UserPO findByMobileNumber(String mobileNumber);
    List<UserPO> findByActivationStatusOrderByFirstNameAsc(ActivationStatus activationStatus);
    List<UserPO> findByUserRoleAndActivationStatus(UserRole userRole, ActivationStatus activationStatus);
}
