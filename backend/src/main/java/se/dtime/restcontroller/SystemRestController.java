package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.SystemConfiguration;
import se.dtime.model.SystemPropertyDB;
import se.dtime.service.system.SystemOperationService;
import se.dtime.service.system.SystemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/system")
public class SystemRestController {

    @Autowired
    private SystemService systemService;
    @Autowired
    private SystemOperationService systemOperationService;

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

}