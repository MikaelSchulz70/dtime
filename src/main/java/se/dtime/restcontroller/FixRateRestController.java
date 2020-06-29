package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.FixRate;
import se.dtime.service.rate.FixRateService;
import se.dtime.service.rate.FixRateValidator;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/fixrates")
public class FixRateRestController {

    @Autowired
    private FixRateService fixRateService;
    @Autowired
    private FixRateValidator fixRateValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity add(@Valid @RequestBody FixRate fixRate) {
        if (fixRate.getId() == 0) {
            fixRateService.add(fixRate);
        } else {
            fixRateService.update(fixRate);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<FixRate[]> getCurrentRates() {
        return new ResponseEntity<>(fixRateService.getCurrentFixRates(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{idProject}")
    public ResponseEntity<FixRate[]> getRatesForProject(@PathVariable long idProject) {
        FixRate[] rates = fixRateService.getRatesForProject(idProject);
        return new ResponseEntity<>(rates, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        fixRateValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        fixRateService.delete(id);
    }
}
