package se.dtime.service.task;

import org.springframework.stereotype.Service;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.Task;
import se.dtime.service.BaseConverter;
import se.dtime.service.account.AccountConverter;

import java.util.List;

@Service
public class TaskConverter extends BaseConverter {

    private final AccountConverter accountConverter;

    public TaskConverter(AccountConverter accountConverter) {
        this.accountConverter = accountConverter;
    }

    public Task toModel(TaskPO taskPO) {
        if (taskPO == null) {
            return null;
        }

        return Task.builder().id(taskPO.getId()).
                name(taskPO.getName()).
                activationStatus(taskPO.getActivationStatus()).
                taskType(taskPO.getTaskType()).
                isBillable(taskPO.getIsBillable()).
                account(accountConverter.toModel(taskPO.getAccount())).
                build();
    }

    public TaskPO toPO(Task task) {
        if (task == null) {
            return null;
        }

        TaskPO taskPO = new TaskPO();
        // Only set ID if it's not 0 (for updates)
        // For new entities (ID = 0), let Hibernate generate the ID via sequence
        if (task.getId() != null && task.getId() != 0) {
            taskPO.setId(task.getId());
        }
        taskPO.setName(task.getName());
        taskPO.setActivationStatus(task.getActivationStatus());
        taskPO.setTaskType(task.getTaskType());
        taskPO.setIsBillable(task.getIsBillable() != null ? task.getIsBillable() : false);
        updateBaseData(taskPO);

        AccountPO accountPO = new AccountPO();
        accountPO.setId(task.getAccount().getId());
        taskPO.setAccount(accountPO);

        return taskPO;
    }

    public Task[] toModel(List<TaskPO> taskPOList) {
        List<Task> categories = taskPOList.stream()
                .map(this::toModel)
                .toList();
        return categories.toArray(new Task[0]);
    }
}
