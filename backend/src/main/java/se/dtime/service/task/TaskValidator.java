package se.dtime.service.task;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.common.AttributeValidator;
import se.dtime.common.ValidatorBase;
import se.dtime.dbmodel.AccountPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.Attribute;
import se.dtime.model.Task;
import se.dtime.model.TaskType;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.TimeEntryRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskValidator extends ValidatorBase<Task> {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    static final String FIELD_NAME = "name";
    static final String FIELD_ACTIVATION_STATUS = "activationStatus";

    private static Map<String, AttributeValidator> VALIDATOR_MAP;

    @PostConstruct
    public void init() {
        if (VALIDATOR_MAP == null) {
            VALIDATOR_MAP = new HashMap<>();
            VALIDATOR_MAP.put(FIELD_NAME, new NameValidator());
            VALIDATOR_MAP.put(FIELD_ACTIVATION_STATUS, new ActivationStatusValidator());
        }
    }

    @Override
    public void validateAdd(Task task) {
        validateName(task);
        validateTaskType(task);
    }

    @Override
    public void validateDelete(long taskId) {
        TaskPO taskPO = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("task.not.found"));
        validateHasTimeEntries(taskPO);
    }

    @Override
    public void validateUpdate(Task task) {
        TaskPO taskPO = taskRepository.findById(task.getId()).orElseThrow(() -> new NotFoundException("task.not.found"));

        validateName(task);
        validateTaskType(task);
    }

    private void validateName(Task task) {
        List<TaskPO> taskPOS = taskRepository.findByName(task.getName());
        check(taskPOS == null || taskPOS.isEmpty() ||
                        taskPOS.stream().noneMatch(c -> !c.getId().equals(task.getId()) &&
                                c.getAccount().getId().equals(task.getAccount().getId())),
                "task.name.not.unique");
    }

    private void validateTaskType(Task task) {
        TaskType taskType = task.getTaskType();
        if (taskType != TaskType.NORMAL) {
            AccountPO accountPO = new AccountPO(task.getAccount().getId());
            List<TaskPO> existingTasks = taskRepository.findByTaskTypeAndAccount(taskType, accountPO);
            boolean hasExisting = existingTasks != null &&
                    existingTasks.stream().anyMatch(t -> !t.getId().equals(task.getId()));

            if (hasExisting) {
                String errorKey = getTaskTypeErrorKey(taskType);
                check(false, errorKey);
            }
        }
    }

    private String getTaskTypeErrorKey(TaskType taskType) {
        return switch (taskType) {
            case VACATION -> "task.vacation.already.exists";
            case SICK_LEAVE -> "task.sick.leave.already.exists";
            case PARENTAL_LEAVE -> "task.parental.leave.already.exists";
            default -> "task.type.already.exists";
        };
    }

    private void validateHasTimeEntries(TaskPO taskPO) {
        BigDecimal totalTime = timeEntryRepository.sumReportedTimeByTask(taskPO.getId());
        check(totalTime == null || totalTime.compareTo(BigDecimal.ZERO) == 0, "task.cannot.delete.have.time.entries");
    }

    public void validate(Attribute attribute) throws ValidationException {
        AttributeValidator validator = VALIDATOR_MAP.get(attribute.getName());
        if (validator != null) {
            validator.validate(attribute);
        }
    }

    class NameValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 40, "task.name.length");
        }
    }

    class ActivationStatusValidator extends AttributeValidator {
        @Override
        public void validate(Attribute attribute) throws ValidationException {
            checkLength(attribute, 1, 30, "activation.status.invalid");

            try {
                ActivationStatus.valueOf(attribute.getValue());
            } catch (IllegalArgumentException e) {
                check(false, FIELD_ACTIVATION_STATUS, "activation.status.invalid");
            }
        }
    }
}