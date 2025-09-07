package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.report.Report;
import se.dtime.model.report.ReportType;
import se.dtime.model.report.ReportView;
import se.dtime.model.timereport.CloseDate;
import se.dtime.service.report.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/report")
public class ReportRestController {
    
    private final ReportService reportService;

    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Report> getCurrentReports(@RequestParam(value = "view", required = false) ReportView reportView,
                                                    @RequestParam(value = "type", required = false) ReportType reportType) {
        Report report = reportService.getCurrentReports(reportView, reportType);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/previous")
    public ResponseEntity<Report> getPreviousReport(@RequestParam(value = "view", required = false) ReportView reportView,
                                                    @RequestParam(value = "type", required = false) ReportType reportType,
                                                    @RequestParam(value = "date", required = true)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Report report = reportService.getPreviousReport(reportView, reportType, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/next")
    public ResponseEntity<Report> getNextReport(@RequestParam(value = "view", required = false) ReportView reportView,
                                                @RequestParam(value = "type", required = false) ReportType reportType,
                                                @RequestParam(value = "date", required = true)
                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Report report = reportService.getNextReport(reportView, reportType, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user")
    public ResponseEntity<Report> getUserReport(@RequestParam(value = "view", required = false) ReportView reportView) {
        Report report = reportService.getUserReport(reportView);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/next")
    public ResponseEntity<Report> getNextUserReport(@RequestParam(value = "view", required = false) ReportView reportView,
                                                    @RequestParam(value = "date", required = true)
                                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Report report = reportService.getNextUserReport(reportView, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/previous")
    public ResponseEntity<Report> getPreviousUserReport(@RequestParam(value = "view", required = false) ReportView reportView,
                                                        @RequestParam(value = "date", required = true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Report report = reportService.getPreviousUserReport(reportView, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/close")
    public void closeTimeReport(@Valid @RequestBody CloseDate closeDate) {
        reportService.closeTimeReport(closeDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/open")
    public void openTimeReport(@Valid @RequestBody CloseDate closeDate) {
        reportService.openTimeReport(closeDate);
    }
}
