package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.dtime.model.SessionInfo;
import se.dtime.service.admin.SessionService;

@RestController
@RequestMapping("/api/session")
public class SessionRestController {

    @Autowired
    private SessionService sessionService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping(path = "")
    public ResponseEntity<SessionInfo> getSessionInfo() {
        return new ResponseEntity<>(sessionService.getSessionInfo(), HttpStatus.OK);
    }
}