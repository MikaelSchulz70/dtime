package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import se.dtime.model.Attribute;
import se.dtime.model.User;
import se.dtime.model.UserPwd;
import se.dtime.service.user.UserService;
import se.dtime.service.user.UserValidator;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserValidator userValidator;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "")
    public ResponseEntity<Void> addOrUpdate(@Valid @RequestBody User user) {
        if (user.getId() == 0) {
            userService.add(user);
        } else {
            userService.update(user);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "")
    public ResponseEntity<User[]> getAll(@RequestParam(value = "active", required = false) Boolean active) {
        return new ResponseEntity<>(userService.getAll(active), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public ResponseEntity<User> get(@PathVariable long id) {
        User user = userService.get(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/validate")
    public void validate(@RequestBody Attribute attribute) {
        userValidator.validate(attribute);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping(path = "/changepwd")
    public void changePwd(@Valid @RequestBody UserPwd userPwd) {
        userService.changePwd(userPwd);
    }
}