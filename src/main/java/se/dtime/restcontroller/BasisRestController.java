package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.basis.InvoiceBasis;
import se.dtime.model.basis.MonthlyCheck;
import se.dtime.service.basis.BasisService;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/basis")
public class BasisRestController {

    @Autowired
    private BasisService basisService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/invoice/current")
    public ResponseEntity<InvoiceBasis> getCurrentInvoiceBasis() {
        return new ResponseEntity<>(basisService.getCurrentInvoiceBasis(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/invoice/next")
    public ResponseEntity<InvoiceBasis> getNextInvoiceBasis(@RequestParam(value = "date", required = true)
                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(basisService.getNextInvoiceBasis(date), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/invoice/previous")
    public ResponseEntity<InvoiceBasis> getPreviousInvoiceBasis(@RequestParam(value = "date", required = true)
                                                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(basisService.getPreviousInvoiceBasis(date), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/invoice/monthlycheck")
    public ResponseEntity<MonthlyCheck> addUpdateMontlyCheck(@Valid @RequestBody MonthlyCheck monthlyCheck) {
        return new ResponseEntity<>(basisService.addUpdateMonthlyCheck(monthlyCheck), HttpStatus.OK);
    }
}
