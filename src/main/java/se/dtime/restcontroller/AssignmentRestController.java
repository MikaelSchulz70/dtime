package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Assignment;
import se.dtime.model.Attribute;
import se.dtime.service.assignment.AssignmentService;
import se.dtime.service.assignment.AssignmentValidator;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentRestController {

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private AssignmentValidator assignmentValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody Assignment assignment) {
        assignmentService.addOrUpdate(assignment);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{idUser}")
    public ResponseEntity<List<Assignment>> get(@PathVariable long idUser) {
        List<Assignment> assignments = assignmentService.getAssignmentsForUser(idUser);
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/currentassignments")
    public ResponseEntity<List<Assignment>> getCurrentAssignments() {
        List<Assignment> assignments = assignmentService.getCurrentAssignments();
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        assignmentValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        assignmentService.delete(id);
    }
}
