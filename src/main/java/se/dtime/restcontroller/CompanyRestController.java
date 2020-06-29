package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.Company;
import se.dtime.service.company.CompanyService;
import se.dtime.service.company.CompanyValidator;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyRestController {

    @Autowired
    private CompanyService companyService;
    @Autowired
    private CompanyValidator companyValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity addOrUpdate(@Valid @RequestBody Company company) {
        if (company.getId() == 0) {
            companyService.add(company);
        } else {
            companyService.update(company);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Company[]> getAll(@RequestParam(value = "active", required=false) Boolean active) {
        return new ResponseEntity<>(companyService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<Company> get(@PathVariable long id) {
        Company company = companyService.get(id);
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        companyValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        companyService.delete(id);
    }
}
