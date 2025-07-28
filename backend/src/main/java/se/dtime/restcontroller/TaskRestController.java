package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.Task;
import se.dtime.service.task.TaskService;
import se.dtime.service.task.TaskValidator;

@RestController
@RequestMapping("/api/task")
public class TaskRestController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskValidator taskValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody Task task) {
        if (task.getId() == 0) {
            taskService.add(task);
        } else {
            taskService.update(task);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Task[]> getAll(@RequestParam(value = "active", required = false) Boolean active) {
        return new ResponseEntity<>(taskService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<Task> get(@PathVariable long id) {
        Task task = taskService.get(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        taskValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        taskService.delete(id);
    }
}
