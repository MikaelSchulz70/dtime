package se.dtime.service.taskcontributor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.TaskContributor;
import se.dtime.model.UserExt;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.UserRepository;

import java.util.List;

@Service
public class TaskContributorService {
    private final TaskContributorConverter taskContributorConverter;
    private final TaskContributorRepository taskContributorRepository;
    private final TaskContributorValidator taskContributorValidator;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskContributorService(TaskContributorConverter taskContributorConverter, TaskContributorRepository taskContributorRepository, TaskContributorValidator taskContributorValidator, TaskRepository taskRepository, UserRepository userRepository) {
        this.taskContributorConverter = taskContributorConverter;
        this.taskContributorRepository = taskContributorRepository;
        this.taskContributorValidator = taskContributorValidator;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskContributor addOrUpdate(TaskContributor taskContributor) {
        taskContributorValidator.validateAdd(taskContributor);
        TaskContributorPO taskContributorPO = taskContributorConverter.toPO(taskContributor);

        TaskContributorPO storedTaskContributorPO = taskContributorRepository.findByUserAndTask(taskContributorPO.getUser(), taskContributorPO.getTask());
        if (storedTaskContributorPO != null) {
            storedTaskContributorPO.setActivationStatus(taskContributor.getActivationStatus());
            taskContributorPO = storedTaskContributorPO;
        }

        TaskContributorPO savedPO = taskContributorRepository.save(taskContributorPO);
        return taskContributorConverter.toModel(savedPO);
    }

    public List<TaskContributor> getTasksForUser(long userId) {
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));
        List<TaskContributorPO> taskContributorPOS = taskContributorRepository.findByUserAndActivationStatus(userPO, ActivationStatus.ACTIVE);
        List<TaskPO> taskPOS = taskRepository.findByActivationStatus(ActivationStatus.ACTIVE);
        return taskContributorConverter.toModel(userPO, taskContributorPOS, taskPOS);
    }

    public List<TaskContributor> getCurrentTaskContributors() {
        return getTasksForUser(resolveAuthenticatedUser().getId());
    }

    public TaskContributor selfAssignTask(long taskId) {
        UserPO currentUser = resolveAuthenticatedUser();
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(se.dtime.model.User.builder().id(currentUser.getId()).build())
                .task(se.dtime.model.Task.builder().id(taskId).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();
        return addOrUpdate(taskContributor);
    }

    public TaskContributor selfUnassignTask(long taskId) {
        UserPO currentUser = resolveAuthenticatedUser();
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(se.dtime.model.User.builder().id(currentUser.getId()).build())
                .task(se.dtime.model.Task.builder().id(taskId).build())
                .activationStatus(ActivationStatus.INACTIVE)
                .build();
        return addOrUpdate(taskContributor);
    }

    private UserPO resolveAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserExt userExt) {
            return userRepository.findById(userExt.getId()).orElseThrow(() -> new NotFoundException("user.not.found"));
        }

        if (principal instanceof OAuth2User oauth2User) {
            String sub = oauth2User.getAttribute("sub");
            if (sub != null && !sub.isBlank()) {
                UserPO userByExternalId = userRepository.findByExternalId(sub);
                if (userByExternalId != null) {
                    return userByExternalId;
                }
            }

            String email = oauth2User.getAttribute("email");
            if (email != null && !email.isBlank()) {
                UserPO userByEmail = userRepository.findByEmail(email);
                if (userByEmail != null) {
                    return userByEmail;
                }
            }
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.isBlank()) {
            UserPO userByUsername = userRepository.findByEmail(username);
            if (userByUsername != null) {
                return userByUsername;
            }
        }

        throw new NotFoundException("user.not.found");
    }

    public void delete(long id) {
        taskContributorRepository.deleteById(id);
    }
}
