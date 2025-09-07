package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.service.basis.BasisService;

@RestController
@RequestMapping("/api/basis")
public class BasisRestController {

    private final BasisService basisService;

    public BasisRestController(BasisService basisService) {
        this.basisService = basisService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/invoice/monthlycheck")
    public ResponseEntity<MonthlyCheck> addUpdateMonthlyCheck(@Valid @RequestBody MonthlyCheck monthlyCheck) {
        return new ResponseEntity<>(basisService.addUpdateMonthlyCheck(monthlyCheck), HttpStatus.OK);
    }
}
