package se.dtime.service.taskcontributor;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.TaskContributor;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.UserRepository;
import se.dtime.service.user.CurrentUserResolver;

import java.util.List;

@Service
public class TaskContributorService {
    private final TaskContributorConverter taskContributorConverter;
    private final TaskContributorRepository taskContributorRepository;
    private final TaskContributorValidator taskContributorValidator;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;

    public TaskContributorService(TaskContributorConverter taskContributorConverter, TaskContributorRepository taskContributorRepository, TaskContributorValidator taskContributorValidator, TaskRepository taskRepository, UserRepository userRepository, CurrentUserResolver currentUserResolver) {
        this.taskContributorConverter = taskContributorConverter;
        this.taskContributorRepository = taskContributorRepository;
        this.taskContributorValidator = taskContributorValidator;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.currentUserResolver = currentUserResolver;
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
        return getTasksForUser(currentUserResolver.resolveCurrentUser().getId());
    }

    public TaskContributor selfAssignTask(long taskId) {
        UserPO currentUser = currentUserResolver.resolveCurrentUser();
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(se.dtime.model.User.builder().id(currentUser.getId()).build())
                .task(se.dtime.model.Task.builder().id(taskId).build())
                .activationStatus(ActivationStatus.ACTIVE)
                .build();
        return addOrUpdate(taskContributor);
    }

    public TaskContributor selfUnassignTask(long taskId) {
        UserPO currentUser = currentUserResolver.resolveCurrentUser();
        TaskContributor taskContributor = TaskContributor.builder()
                .id(0L)
                .user(se.dtime.model.User.builder().id(currentUser.getId()).build())
                .task(se.dtime.model.Task.builder().id(taskId).build())
                .activationStatus(ActivationStatus.INACTIVE)
                .build();
        return addOrUpdate(taskContributor);
    }

    public void delete(long id) {
        taskContributorRepository.deleteById(id);
    }
}
