package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Account;
import se.dtime.model.Attribute;
import se.dtime.model.PagedResponse;
import se.dtime.service.account.AccountService;
import se.dtime.service.account.AccountValidator;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    private final AccountService accountService;
    private final AccountValidator accountValidator;

    public AccountRestController(AccountService accountService, AccountValidator accountValidator) {
        this.accountService = accountService;
        this.accountValidator = accountValidator;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity<Void> create(@Valid @RequestBody Account account) {
        accountService.add(account);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping()
    public ResponseEntity<Void> update(@Valid @RequestBody Account account) {
        accountService.update(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<Account[]> getAll(@RequestParam(value = "active", required = false) Boolean active) {
        return new ResponseEntity<>(accountService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/paged")
    public ResponseEntity<PagedResponse<Account>> getAllPaged(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "name", required = false) String name) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PagedResponse<Account> response = accountService.getAllPaged(pageable, active, name);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
