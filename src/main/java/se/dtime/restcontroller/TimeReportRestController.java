package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.timereport.TimeReport;
import se.dtime.model.timereport.TimeReportDay;
import se.dtime.model.timereport.TimeReportView;
import se.dtime.model.timereport.Vacations;
import se.dtime.service.calendar.CalendarService;
import se.dtime.service.timereport.TimeReportService;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/timereport")
public class TimeReportRestController {
    @Autowired
    private TimeReportService timeReportService;
    @Autowired
    private CalendarService calendarService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "")
    public long addOrUpdate(@Valid @RequestBody TimeReportDay timeReportDay) {
        return timeReportService.addOrUpdate(timeReportDay);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "")
    public ResponseEntity<TimeReport> getCurrentTimeReport(@RequestParam(value = "view", required=false) TimeReportView timeReportView) {
        TimeReport timeReport = timeReportService.getCurrentTimeReport(timeReportView);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/previous")
    public ResponseEntity<TimeReport> getPreviousTimeReport(@RequestParam(value = "view", required=false) TimeReportView timeReportView,
                                                            @RequestParam(value = "date", required=true)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeReport timeReport = timeReportService.getPreviousTimeReport(timeReportView, date);
        return new ResponseEntity<>(timeReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/next")
    public ResponseEntity<TimeReport> getNextTimeReport(@RequestParam(value = "view", required=false) TimeReportView timeReportView,
                                                        @RequestParam(value = "date", required=true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeReport timeReportDay = timeReportService.getNextTimeReport(timeReportView, date);
        return new ResponseEntity<>(timeReportDay, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/vacations")
    public ResponseEntity<Vacations> getCurrentVacations() {
        Vacations vacations = timeReportService.getCurrentVacations();
        return new ResponseEntity<>(vacations, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/vacations/previous")
    public ResponseEntity<Vacations> getPreviousVacations(@RequestParam(value = "date", required=true)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Vacations vacations = timeReportService.getPreviousVacations(date);
        return new ResponseEntity<>(vacations, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/vacations/next")
    public ResponseEntity<Vacations> getNextVacations(@RequestParam(value = "date", required=true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Vacations vacations = timeReportService.getNextVacations(date);
        return new ResponseEntity<>(vacations, HttpStatus.OK);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/user")
    public ResponseEntity<TimeReport> getUserTimeReport(@RequestParam(value = "idUser", required=true) long idUser,
                                                        @RequestParam(value = "view", required=true) TimeReportView timeReportView,
                                                        @RequestParam(value = "date", required=true)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeReport timeReportDay = timeReportService.getUserTimeReport(idUser, timeReportView, date);
        return new ResponseEntity<>(timeReportDay, HttpStatus.OK);
    }
}
