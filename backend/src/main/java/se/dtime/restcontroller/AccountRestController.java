package se.dtime.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Account;
import se.dtime.model.Attribute;
import se.dtime.service.account.AccountService;
import se.dtime.service.account.AccountValidator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountValidator accountValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity addOrUpdate(@Valid @RequestBody Account account) {
        if (account.getId() == 0) {
            accountService.add(account);
        } else {
            accountService.update(account);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Account[]> getAll(@RequestParam(value = "active", required = false) Boolean active) {
        return new ResponseEntity<>(accountService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<Account> get(@PathVariable long id) {
        Account account = accountService.get(id);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        accountValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        accountService.delete(id);
    }
}
