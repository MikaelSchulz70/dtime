package se.dtime.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.TaskContributorPO;
import se.dtime.dbmodel.TaskPO;
import se.dtime.model.ActivationStatus;
import se.dtime.model.PagedResponse;
import se.dtime.model.Task;
import se.dtime.model.error.NotFoundException;
import se.dtime.repository.TaskContributorRepository;
import se.dtime.repository.TaskRepository;
import se.dtime.repository.TimeEntryRepository;

import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskConverter taskConverter;
    @Autowired
    private TaskValidator taskValidator;
    @Autowired
    private TaskContributorRepository taskContributorRepository;
    @Autowired
    private TimeEntryRepository timeEntryRepository;

    public Task add(Task task) {
        taskValidator.validateAdd(task);
        TaskPO taskPO = taskConverter.toPO(task);
        TaskPO savedPO = taskRepository.save(taskPO);
        return taskConverter.toModel(savedPO);
    }

    public void update(Task task) {
        taskValidator.validateUpdate(task);
        TaskPO updatedTask = taskConverter.toPO(task);
        taskRepository.save(updatedTask);

        if (task.getActivationStatus() == ActivationStatus.INACTIVE) {
            List<TaskContributorPO> taskContributorPOS = taskContributorRepository.findByTask(updatedTask);
            taskContributorPOS.forEach(p -> p.setActivationStatus(ActivationStatus.INACTIVE));
        }
    }


    public Task[] getAll(Boolean active) {
        List<TaskPO> taskPOS = null;

        if (active == null) {
            taskPOS = taskRepository.findAll(Sort.by("account.name").ascending().and(Sort.by("name").ascending()));
        } else {
            ActivationStatus activationStatus = (active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE);
            taskPOS = taskRepository.findByActivationStatus(activationStatus);
        }

        return taskConverter.toModel(taskPOS);
    }

    public PagedResponse<Task> getAllPaged(Pageable pageable, Boolean active, String name, Long accountId) {
        Page<TaskPO> page;
        
        if (active != null && accountId != null) {
            ActivationStatus activationStatus = active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE;
            page = taskRepository.findByActivationStatusAndAccountId(pageable, activationStatus, accountId);
        } else if (active != null) {
            ActivationStatus activationStatus = active ? ActivationStatus.ACTIVE : ActivationStatus.INACTIVE;
            page = taskRepository.findByActivationStatus(pageable, activationStatus);
        } else if (accountId != null) {
            page = taskRepository.findByAccountId(pageable, accountId);
        } else {
            page = taskRepository.findAll(pageable);
        }
        
        // Apply name filter if provided
        List<TaskPO> filteredTasks = page.getContent();
        if (name != null && !name.isEmpty()) {
            filteredTasks = page.getContent().stream()
                    .filter(t -> t.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }
        
        Task[] tasks = taskConverter.toModel(filteredTasks);
        
        return new PagedResponse<>(
            Arrays.asList(tasks),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getSize(),
            page.isFirst(),
            page.isLast()
        );
    }

    public Task get(long id) {
        TaskPO taskPO = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("task.not.found"));
        return taskConverter.toModel(taskPO);
    }

    public void delete(long taskId) {
        taskValidator.validateDelete(taskId);

        TaskPO taskPO = new TaskPO(taskId);
        timeEntryRepository.deleteAll(timeEntryRepository.findByTask(taskId));
        taskContributorRepository.deleteAll(taskContributorRepository.findByTask(taskPO));
        taskRepository.deleteById(taskId);
    }
}
