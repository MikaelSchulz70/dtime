package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.Project;
import se.dtime.service.project.ProjectService;
import se.dtime.service.project.ProjectValidator;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectRestController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectValidator projectValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody Project project) {
        if (project.getId() == 0) {
            projectService.add(project);
        } else {
            projectService.update(project);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Project[]> getAll(@RequestParam(value = "active", required=false) Boolean active,
                                            @RequestParam(value = "onCall", required=false) Boolean onCall) {
        return new ResponseEntity<>(projectService.getAll(active, onCall), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<Project> get(@PathVariable long id) {
        Project project = projectService.get(id);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        projectValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        projectService.delete(id);
    }
}
