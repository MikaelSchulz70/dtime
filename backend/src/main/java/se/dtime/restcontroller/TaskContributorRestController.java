package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.TaskContributor;
import se.dtime.service.taskcontributor.TaskContributorService;
import se.dtime.service.taskcontributor.TaskContributorValidator;

import java.util.List;

@RestController
@RequestMapping("/api/taskcontributor")
public class TaskContributorRestController {

    private final TaskContributorService taskContributorService;
    private final TaskContributorValidator taskContributorValidator;

    public TaskContributorRestController(TaskContributorService taskContributorService, 
                                       TaskContributorValidator taskContributorValidator) {
        this.taskContributorService = taskContributorService;
        this.taskContributorValidator = taskContributorValidator;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity<Void> add(@Valid @RequestBody TaskContributor taskContributor) {
        taskContributorService.addOrUpdate(taskContributor);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<List<TaskContributor>> get(@PathVariable long userId) {
        List<TaskContributor> taskContributors = taskContributorService.getTasksForUser(userId);
        return new ResponseEntity<>(taskContributors, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/currentTaskContributors")
    public ResponseEntity<List<TaskContributor>> getCurrentTaskContributor() {
        List<TaskContributor> taskContributors = taskContributorService.getCurrentTaskContributors();
        return new ResponseEntity<>(taskContributors, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        taskContributorValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        taskContributorService.delete(id);
    }
}
