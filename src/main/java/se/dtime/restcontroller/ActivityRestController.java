package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Activity;
import se.dtime.service.activity.ActivityService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/activities")
public class ActivityRestController {
    @Autowired
    private ActivityService activityService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "")
    public long addOrUpdate(@Valid @RequestBody Activity activity) {
        return activityService.addOrUpdate(activity);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "")
    public ResponseEntity<Activity[]> getActivities() {
        Activity[] activities = activityService.getAll();
        return new ResponseEntity<>(activities, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/vote/{id}")
    public void voteOrUnVote(@PathVariable long id) {
        activityService.voteOrUnVote(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/resetvotes")
    public void reset() {
        activityService.reset();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        activityService.delete(id);
    }
}
