package se.dtime.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.PagedResponse;
import se.dtime.model.User;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter userConverter;
    private final UserValidator userValidator;
    private final TaskContributorRepository taskContributorRepository;

    public UserService(UserRepository userRepository, UserConverter userConverter, UserValidator userValidator, TaskContributorRepository taskContributorRepository) {
        this.userRepository = userRepository;
        this.userConverter = userConverter;
        this.userValidator = userValidator;
        this.taskContributorRepository = taskContributorRepository;
    }

    public void deactivate(long userId) {
        userValidator.validateDeactivate(userId);
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));

        userPO.setActivationStatus(ActivationStatus.INACTIVE);
        List<TaskContributorPO> taskContributorPOS = taskContributorRepository.findByUser(userPO);
        taskContributorPOS.forEach(a -> a.setActivationStatus(ActivationStatus.INACTIVE));
        userRepository.save(userPO);
    }

    public void activate(long userId) {
        userValidator.validateActivate(userId);
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));
        userPO.setActivationStatus(ActivationStatus.ACTIVE);
        userRepository.save(userPO);
    }

    public User[] getAll(Boolean active) {
        List<UserPO> userPOS;
        if (active == null) {
            userPOS = userRepository.findAll(Sort.by("displayName").ascending());
        } else if (active) {
            userPOS = userRepository.findByActivationStatusOrderByDisplayNameAsc(ActivationStatus.ACTIVE);
        } else {
            userPOS = userRepository.findByActivationStatusOrderByDisplayNameAsc(ActivationStatus.INACTIVE);
        }

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

        List<UserPO> filteredUserPOS = page.getContent().stream()
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
        UserPO userPO = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user.not.found"));
        return userConverter.toModel(userPO);
    }
}
