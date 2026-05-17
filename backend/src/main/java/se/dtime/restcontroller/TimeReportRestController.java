package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.timereport.TimeEntry;
import se.dtime.model.timereport.TimeReport;
import se.dtime.model.timereport.TimeReportView;
import se.dtime.model.timereport.VacationReport;
import se.dtime.service.timeentry.TimeEntryService;

import java.time.LocalDate;

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
    @GetMapping(path = "")
    public ResponseEntity<TimeReport> getTimeReport(
            @RequestParam(value = "view", required = false) TimeReportView timeReportView,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeReport timeReport = timeEntryService.getTimeReport(timeReportView, date);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping(path = "/user")
    public ResponseEntity<TimeReport> getUserTimeReport(@RequestParam(value = "userId", required = true) long userId,
                                                        @RequestParam(value = "view", required = true) TimeReportView timeReportView,
                                                        @RequestParam(value = "date", required = true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeReport timeReportDay = timeEntryService.getUserTimeReport(userId, timeReportView, date);
        return new ResponseEntity<>(timeReportDay, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/vacations")
    public ResponseEntity<VacationReport> getVacations(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        VacationReport vacationReport = timeEntryService.getVacationReport(date);
        return new ResponseEntity<>(vacationReport, HttpStatus.OK);
    }
}
