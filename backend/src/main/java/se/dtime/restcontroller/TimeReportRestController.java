package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.timereport.TimeEntry;
import se.dtime.model.timereport.TimeReport;
import se.dtime.model.timereport.VacationReport;
import se.dtime.service.timeentry.TimeEntryService;

@RestController
@RequestMapping("/api/timereport")
public class TimeReportRestController {
    private final TimeEntryService timeEntryService;

    public TimeReportRestController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "")
    public long addOrUpdate(@Valid @RequestBody TimeEntry timeEntry) {
        return timeEntryService.addOrUpdate(timeEntry);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/year/{year}/month/{month}")
    public ResponseEntity<TimeReport> getTimeReportForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        TimeReport timeReport = timeEntryService.getTimeReportForMonth(year, month);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/year/{year}/week/{week}")
    public ResponseEntity<TimeReport> getTimeReportForWeek(
            @PathVariable int year,
            @PathVariable int week) {
        TimeReport timeReport = timeEntryService.getTimeReportForWeek(year, week);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/user/{userId}/year/{year}/month/{month}")
    public ResponseEntity<TimeReport> getUserTimeReportForMonth(
            @PathVariable long userId,
            @PathVariable int year,
            @PathVariable int month) {
        TimeReport timeReport = timeEntryService.getUserTimeReportForMonth(userId, year, month);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/user/{userId}/year/{year}/week/{week}")
    public ResponseEntity<TimeReport> getUserTimeReportForWeek(
            @PathVariable long userId,
            @PathVariable int year,
            @PathVariable int week) {
        TimeReport timeReport = timeEntryService.getUserTimeReportForWeek(userId, year, week);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/vacations/year/{year}/month/{month}")
    public ResponseEntity<VacationReport> getVacationsForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        VacationReport vacationReport = timeEntryService.getVacationReportForMonth(year, month);
        return new ResponseEntity<>(vacationReport, HttpStatus.OK);
    }
}
