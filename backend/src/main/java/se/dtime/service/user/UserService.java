package se.dtime.service.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.User;
import se.dtime.model.UserPwd;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private TimeEntryRepository timeEntryRepository;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private CloseDateRepository closeDateRepository;

    public User add(User user) {
        userValidator.validateAdd(user);
        UserPO userPO = userConverter.toPO(user);
        UserPO savedPO = userRepository.save(userPO);
        return userConverter.toModel(savedPO);
    }

    public void update(User user) {
        userValidator.validateUpdate(user);
        UserPO currentUser = userRepository.findById(user.getId()).orElseThrow(() -> new NotFoundException("user.not.found"));
        UserPO userPO = userConverter.toPO(user, currentUser);

        if (user.getActivationStatus() == ActivationStatus.INACTIVE && currentUser.getActivationStatus() == ActivationStatus.ACTIVE) {
            List<TaskContributorPO> taskContributorPOS = taskContributorRepository.findByUser(userPO);
            taskContributorPOS.forEach(a -> a.setActivationStatus(ActivationStatus.INACTIVE));
        }

        userRepository.save(userPO);
    }

    public User[] getAll(Boolean active) {
        List<UserPO> userPOS;
        if (active == null) {
            userPOS = userRepository.findAll(Sort.by("firstName").ascending().and(Sort.by("lastName").ascending()));
        } else if (active) {
            userPOS = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.ACTIVE);
        } else {
            userPOS = userRepository.findByActivationStatusOrderByFirstNameAsc(ActivationStatus.INACTIVE);
        }

        userPOS = userPOS.stream()
                .filter(u -> u.getId() != CommonData.SYSTEM_USER_ID)
                .toList();

        return userConverter.toModel(userPOS);
    }

    public User get(long id) {
        if (id == CommonData.SYSTEM_USER_ID) {
            throw new NotFoundException("user.not.found");
        }

        UserPO userPO = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user.not.found"));
        return userConverter.toModel(userPO);
    }

    public void delete(long userId) {
        userValidator.validateDelete(userId);

        UserPO userPO = new UserPO(userId);
        timeEntryRepository.deleteAll(timeEntryRepository.findByUser(userId));
        taskContributorRepository.deleteAll(taskContributorRepository.findByUser(userPO));
        closeDateRepository.deleteAll(closeDateRepository.findByUser(userPO));
        userRepository.deleteById(userId);
    }

    public void changePwd(UserPwd userPwd) {
        userValidator.validateLoggedIn();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserPO user = userRepository.findByEmail(userDetails.getUsername());
        boolean pwdMatches = passwordEncoder.matches(userPwd.getCurrentPassword(), user.getPassword());
        if (!pwdMatches) {
            throw new ValidationException("currentPassword", "user.invalid.current.pwd");
        }

        if (!StringUtils.equals(userPwd.getNewPassword1(), userPwd.getNewPassword2())) {
            throw new ValidationException("newPassword1", "user.new.pwd.do.not.match");
        }

        user.setPassword(passwordEncoder.encode(userPwd.getNewPassword1()));
        user.setUpdatedDateTime(LocalDateTime.now());
        userRepository.save(user);
    }

}
