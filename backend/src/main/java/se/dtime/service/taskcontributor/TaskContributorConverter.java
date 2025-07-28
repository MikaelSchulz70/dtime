package se.dtime.service.taskcontributor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.dbmodel.UserPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.TaskContributor;
import se.dtime.model.User;
import se.dtime.service.BaseConverter;
import se.dtime.service.task.TaskConverter;
import se.dtime.service.user.UserConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskContributorConverter extends BaseConverter {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private TaskConverter taskConverter;

    public TaskContributor toModel(TaskContributorPO taskContributorPO) {
        if (taskContributorPO == null) {
            return null;
        }

        return TaskContributor.builder().id(taskContributorPO.getId()).
                user(userConverter.toModel(taskContributorPO.getUser())).
                task(taskConverter.toModel(taskContributorPO.getTask())).
                activationStatus(taskContributorPO.getActivationStatus()).
                build();
    }

    public List<TaskContributor> toModel(List<TaskContributorPO> taskContributorPOList) {
        return taskContributorPOList.stream().map(p -> toModel(p)).collect(Collectors.toList());
    }

    public TaskContributorPO toPO(TaskContributor taskContributor) {
        if (taskContributor == null) {
            return null;
        }

        TaskContributorPO taskContributorPO = new TaskContributorPO();
        // Only set ID if it's not 0 (for updates)
        // For new entities (ID = 0), let Hibernate generate the ID via sequence
        if (taskContributor.getId() != null && taskContributor.getId() != 0) {
            taskContributorPO.setId(taskContributor.getId());
        }
        taskContributorPO.setUser(new UserPO(taskContributor.getUser().getId()));
        taskContributorPO.setTask(new TaskPO(taskContributor.getTask().getId()));
        taskContributorPO.setActivationStatus(taskContributor.getActivationStatus());
        updateBaseData(taskContributorPO);

        return taskContributorPO;
    }

    public List<TaskContributor> toModel(UserPO userPO, List<TaskContributorPO> taskContributorPOS, List<TaskPO> taskPOS) {
        User user = userConverter.toModel(userPO);

        List<TaskContributor> taskContributors = new ArrayList<>();
        for (TaskPO taskPO : taskPOS) {
            Optional<TaskContributorPO> participationPO = taskContributorPOS.
                    stream().
                    filter(x -> x.getTask().getId() == taskPO.getId()).
                    findFirst();

            TaskContributor taskContributor = TaskContributor.builder().
                    id(0L).
                    task(taskConverter.toModel(taskPO)).
                    user(user).
                    activationStatus(participationPO.isPresent() ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE).
                    build();

            taskContributors.add(taskContributor);
        }

        return taskContributors;
    }
}
