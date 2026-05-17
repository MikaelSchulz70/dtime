package se.dtime.service.user;

import org.springframework.stereotype.Service;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.User;
import se.dtime.model.UserRole;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.UserRepository;

import java.util.List;

@Service
public class UserValidator extends ValidatorBase<User> {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void validateAdd(User user) {
        throw new ValidationException("user.create.not.supported");
    }

    @Override
    public void validateDelete(long userId) {
        throw new ValidationException("user.delete.not.supported");
    }

    @Override
    public void validateUpdate(User user) {
        throw new ValidationException("user.update.not.supported");
    }

    public void validateDeactivate(long userId) {
        UserPO user = requireExistingUser(userId);

        if (user.getActivationStatus() != ActivationStatus.ACTIVE) {
            throw new ValidationException("user.already.inactive");
        }

        ensureAnotherActiveAdminRemainsIfDeactivatingAdmin(user);
    }

    public void validateActivate(long userId) {
        UserPO user = requireExistingUser(userId);

        if (user.getActivationStatus() != ActivationStatus.INACTIVE) {
            throw new ValidationException("user.already.active");
        }
    }

    private UserPO requireExistingUser(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));
    }

    private void ensureAnotherActiveAdminRemainsIfDeactivatingAdmin(UserPO user) {
        if (user.getUserRole() != UserRole.ADMIN) {
            return;
        }

        List<UserPO> activeAdmins =
                userRepository.findByUserRoleAndActivationStatus(UserRole.ADMIN, ActivationStatus.ACTIVE);
        boolean otherAdminExists = activeAdmins.stream().anyMatch(admin -> admin.getId() != user.getId());
        check(otherAdminExists, "user.at.least.one.has.to.be.admin");
    }
}
