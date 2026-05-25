package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.report.BillableTaskTypeReport;
import se.dtime.model.report.Report;
import se.dtime.model.report.ReportType;
import se.dtime.model.timereport.CloseDate;
import se.dtime.service.report.ReportService;
import se.dtime.repository.jdbc.ReportRepository;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
public class ReportRestController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    public ReportRestController(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/year/{year}/month/{month}")
    public ResponseEntity<Report> getReportForMonth(
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam(value = "type", required = false) ReportType reportType) {
        Report report = reportService.getReportForMonth(year, month, reportType);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/year/{year}")
    public ResponseEntity<Report> getReportForYear(
            @PathVariable int year,
            @RequestParam(value = "type", required = false) ReportType reportType) {
        Report report = reportService.getReportForYear(year, reportType);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/year/{year}/month/{month}")
    public ResponseEntity<Report> getUserReportForMonth(
            @PathVariable int year,
            @PathVariable int month) {
        Report report = reportService.getUserReportForMonth(year, month);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/user/year/{year}")
    public ResponseEntity<Report> getUserReportForYear(@PathVariable int year) {
        Report report = reportService.getUserReportForYear(year);
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/billable-task-type")
    public ResponseEntity<List<BillableTaskTypeReport>> getBillableTaskTypeReport(
            @RequestParam(value = "fromDate", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<BillableTaskTypeReport> reports = reportRepository.getBillableTaskTypeReports(fromDate, toDate);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
}
