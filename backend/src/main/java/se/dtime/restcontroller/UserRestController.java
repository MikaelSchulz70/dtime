package se.dtime.restcontroller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.PagedResponse;
import se.dtime.model.User;
import se.dtime.service.user.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<User[]> getAll(@RequestParam(value = "active", required = false) Boolean active) {
        return new ResponseEntity<>(userService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/paged")
    public ResponseEntity<PagedResponse<User>> getAllPaged(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "firstName") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        String sortProperty = mapSortProperty(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortProperty));

        PagedResponse<User> response = userService.getAllPaged(pageable, active, firstName, lastName);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private String mapSortProperty(String requestedSort) {
        if (requestedSort == null || requestedSort.isBlank()) {
            return "displayName";
        }
        return switch (requestedSort) {
            case "firstName", "lastName" -> "displayName";
            default -> requestedSort;
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<User> get(@PathVariable long id) {
        User user = userService.get(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable long id) {
        userService.deactivate(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable long id) {
        userService.activate(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
