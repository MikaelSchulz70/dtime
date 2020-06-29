package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.dtime.model.report.FollowUpReport;
import se.dtime.model.report.FollowUpReportType;
import se.dtime.model.report.ReportView;
import se.dtime.service.report.FollowUpReportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/followup")
public class FollowUpRestController {
    @Autowired
    private FollowUpReportService followUpReportService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<FollowUpReport> getCurrentReport(@RequestParam(value = "view", required = true) ReportView reportView,
                                                           @RequestParam(value = "type", required = true) FollowUpReportType type) {
        FollowUpReport report = followUpReportService.getCurrentReport(reportView, type);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/previous")
    public ResponseEntity<FollowUpReport> getPreviousReport(@RequestParam(value = "view", required = true) ReportView reportView,
                                                            @RequestParam(value = "type", required = true) FollowUpReportType type,
                                                            @RequestParam(value = "date", required = true)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FollowUpReport report = followUpReportService.getPreviousReport(reportView, type, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/next")
    public ResponseEntity<FollowUpReport> getNextReport(@RequestParam(value = "view", required = true) ReportView reportView,
                                                        @RequestParam(value = "type", required = true) FollowUpReportType type,
                                                        @RequestParam(value = "date", required = true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FollowUpReport report = followUpReportService.getNextReport(reportView, type, date);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }
}
