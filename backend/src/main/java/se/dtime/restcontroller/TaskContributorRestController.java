package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TaskContributorService taskContributorService;
    @Autowired
    private TaskContributorValidator taskContributorValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody TaskContributor taskContributor) {
        taskContributorService.addOrUpdate(taskContributor);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{userId}")
    public ResponseEntity<List<TaskContributor>> get(@PathVariable long userId) {
        List<TaskContributor> assignments = taskContributorService.getParticipationsForUser(userId);
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/currentassignments")
    public ResponseEntity<List<TaskContributor>> getCurrentAssignments() {
        List<TaskContributor> assignments = taskContributorService.getCurrentParticipations();
        return new ResponseEntity<>(assignments, HttpStatus.OK);
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
