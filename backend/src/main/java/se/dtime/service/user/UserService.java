package se.dtime.service.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import se.dtime.common.CommonData;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.PagedResponse;
import se.dtime.model.User;
import se.dtime.model.UserPwd;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.CloseDateRepository;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TimeEntryRepository;
import se.dtime.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final UserValidator userValidator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TimeEntryRepository timeEntryRepository;
    private final TaskContributorRepository taskContributorRepository;
    private final CloseDateRepository closeDateRepository;

    public UserService(UserRepository userRepository, UserConverter userConverter, UserValidator userValidator, BCryptPasswordEncoder passwordEncoder, TimeEntryRepository timeEntryRepository, TaskContributorRepository taskContributorRepository, CloseDateRepository closeDateRepository) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
        this.timeEntryRepository = timeEntryRepository;
        this.taskContributorRepository = taskContributorRepository;
        this.closeDateRepository = closeDateRepository;
    }

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

    public PagedResponse<User> getAllPaged(Pageable pageable, Boolean active, String firstName, String lastName) {
        Page<UserPO> page;

        if (active != null) {
            ActivationStatus activationStatus = active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE;
            page = userRepository.findByActivationStatus(pageable, activationStatus);
        } else {
            page = userRepository.findAll(pageable);
        }

        // Filter out system user and apply additional filters
        List<UserPO> filteredUserPOS = page.getContent().stream()
                .filter(u -> u.getId() != CommonData.SYSTEM_USER_ID)
                .filter(u -> firstName == null || firstName.isEmpty() ||
                        u.getFirstName().toLowerCase().contains(firstName.toLowerCase()))
                .filter(u -> lastName == null || lastName.isEmpty() ||
                        u.getLastName().toLowerCase().contains(lastName.toLowerCase()))
                .toList();

        User[] users = userConverter.toModel(filteredUserPOS);

        return new PagedResponse<>(
                Arrays.asList(users),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
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
