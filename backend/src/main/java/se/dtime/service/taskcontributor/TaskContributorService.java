package se.dtime.service.taskcontributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired
    private TaskContributorConverter taskContributorConverter;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private TaskContributorValidator taskContributorValidator;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;

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

    public List<TaskContributor> getParticipationsForUser(long userId) {
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("user.not.found"));
        List<TaskContributorPO> taskContributorPOS = taskContributorRepository.findByUserAndActivationStatus(userPO, ActivationStatus.ACTIVE);
        List<TaskPO> taskPOS = taskRepository.findByActivationStatus(ActivationStatus.ACTIVE);
        return taskContributorConverter.toModel(userPO, taskContributorPOS, taskPOS);
    }

    public List<TaskContributor> getCurrentParticipations() {
        UserExt userExt = (UserExt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getParticipationsForUser(userExt.getId());
    }

    public void delete(long id) {
        taskContributorRepository.deleteById(id);
    }
}
