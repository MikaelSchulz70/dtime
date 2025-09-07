package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.config.EmailSendConfig;
import se.dtime.model.SystemConfiguration;
import se.dtime.model.SystemPropertyDB;
import se.dtime.service.system.SystemOperationService;
import se.dtime.service.system.SystemService;

@RestController
@RequestMapping("/api/system")
public class SystemRestController {

    private final SystemService systemService;
    private final SystemOperationService systemOperationService;
    private final EmailSendConfig emailSendConfig;

    public SystemRestController(SystemService systemService, SystemOperationService systemOperationService, EmailSendConfig emailSendConfig) {
        this.systemService = systemService;
        this.systemOperationService = systemOperationService;
        this.emailSendConfig = emailSendConfig;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/config")
    public ResponseEntity<SystemConfiguration> getSystemConfig() {
        return new ResponseEntity<>(systemService.getSystemConfig(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/systemproperty")
    public ResponseEntity<Void> updateSystemProperty(@Valid @RequestBody SystemPropertyDB systemProperty) {
        systemService.updateSystemProperty(systemProperty);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/emailreminder")
    public void sendMailReminder() {
        systemOperationService.sendMailReminder();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/emailreminder/unclosed")
    public void sendMailReminderToUnclosedUsers() {
        systemOperationService.sendMailReminderToUnclosedUsers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/mail/enabled")
    public ResponseEntity<Boolean> isMailEnabled() {
        return new ResponseEntity<>(emailSendConfig.isMailEnabled(), HttpStatus.OK);
    }

}