package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.oncall.*;
import se.dtime.service.oncall.OnCallOperationService;
import se.dtime.service.oncall.OnCallRuleValidator;
import se.dtime.service.oncall.OnCallService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/oncall")
public class OnCallRestController {
    @Autowired
    private OnCallService onCallService;
    @Autowired
    private OnCallRuleValidator onCallRuleValidator;
    @Autowired
    private OnCallOperationService onCallOperationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public void addOrUpdate(@Valid @RequestBody OnCallDay onCallDay) {
        onCallService.addOrUpdate(onCallDay);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "")
    public ResponseEntity<OnCallReport> getCurrentOnCallReport() {
        OnCallReport onCallReport = onCallService.getCurrentOnCallReport();
        return new ResponseEntity<>(onCallReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/previous")
    public ResponseEntity<OnCallReport> getPreviousOnCallReport(@RequestParam(value = "date", required = true)
                                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        OnCallReport onCallReport = onCallService.getPreviousOnCallReport(date);
        return new ResponseEntity<>(onCallReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/next")
    public ResponseEntity<OnCallReport> getNextOnCallReport(@RequestParam(value = "date", required = true)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        OnCallReport onCallReport = onCallService.getNextOnCallReport(date);
        return new ResponseEntity<>(onCallReport, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/config")
    public ResponseEntity<OnCallConfig> getOnCallConfig() {
        OnCallConfig onCallConfig = onCallService.getOnCallConfig();
        return new ResponseEntity<>(onCallConfig, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/config")
    public void addOrUpdate(@Valid @RequestBody OnCallDayConfig onCallDayConfig) {
        onCallService.addOrUpdate(onCallDayConfig);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/rules")
    public ResponseEntity<List<OnCallRule>> getOnCallRules() {
        List<OnCallRule> onCallRules = onCallService.getOnCallRules();
        return new ResponseEntity<>(onCallRules, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/rules/project/{projectId}")
    public ResponseEntity<OnCallRule> getOnCallRule(@PathVariable long projectId) {
        OnCallRule onCallRule = onCallService.getOnCallRule(projectId);
        return new ResponseEntity<>(onCallRule, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/rules")
    public void addOrUpdate(@Valid @RequestBody OnCallRule onCallRule) {
        onCallService.addOrUpdate(onCallRule);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/rules/validate")
    public void validate(@Valid @RequestBody Attribute attribute) {
        onCallRuleValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/dispatchemails")
    public void dispatchOnCallEmails() {
        onCallOperationService.dispatchOnCallEmails();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/session")
    public OnCallSession getOnCallSession() {
        return onCallOperationService.getOnCallSession();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/alarms")
    public void updateOnCallAlarm(@RequestBody OnCallAlarm onCallAlarm) {
        onCallOperationService.updateOnCallAlarm(onCallAlarm);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/alarms/{idAlarm}")
    public OnCallAlarm getOnCallAlarm(@PathVariable long idAlarm) {
        return onCallOperationService.getOnCallAlarm(idAlarm);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "/alarms")
    public OnCallAlarmContainer getOnCallAlarms() {
        return onCallOperationService.getOnCallAlarms();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/alarms/truncate")
    public void truncateAlarms() {
        onCallOperationService.truncateAlarms();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/alarms/{idAlarm}")
    public void deleteAlarm(@PathVariable long idAlarm) {
        onCallOperationService.deleteAlarm(idAlarm);
    }
}
