package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.Rate;
import se.dtime.service.rate.RateService;
import se.dtime.service.rate.RateValidator;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/rates")
public class RateRestController {

    @Autowired
    private RateService rateService;
    @Autowired
    private RateValidator rateValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody Rate rate) {
        if (rate.getId() == 0) {
            rateService.add(rate);
        } else {
            rateService.update(rate);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Rate[]> getCurrentRates() {
        return new ResponseEntity<>(rateService.getCurrentRates(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{idAssignment}")
    public ResponseEntity<Rate[]> getRatesForAssignment(@PathVariable long idAssignment) {
        Rate[] rates = rateService.getRatesForAssignment(idAssignment);
        return new ResponseEntity<>(rates, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        rateValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        rateService.delete(id);
    }
}
