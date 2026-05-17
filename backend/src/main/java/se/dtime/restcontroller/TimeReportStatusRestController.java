package se.dtime.restcontroller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.report.UnclosedUserReport;
import se.dtime.model.timereport.CloseDate;
import se.dtime.service.report.TimeReportStatusService;
import jakarta.validation.Valid;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/timereportstatus")
public class TimeReportStatusRestController {

    private final TimeReportStatusService timeReportStatusService;

    public TimeReportStatusRestController(TimeReportStatusService timeReportStatusService) {
        this.timeReportStatusService = timeReportStatusService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<UnclosedUserReport> getUnclosedUsers(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UnclosedUserReport report = timeReportStatusService.getUnclosedUsers(date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/close")
    public void closeUserTimeReport(@Valid @RequestBody CloseDate closeDate) {
        timeReportStatusService.closeUserTimeReport(closeDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/open")
    public void openUserTimeReport(@Valid @RequestBody CloseDate closeDate) {
        timeReportStatusService.openUserTimeReport(closeDate);
    }
}
