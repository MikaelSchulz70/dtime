package se.dtime.restcontroller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.dtime.model.SpecialDay;
import se.dtime.service.specialday.SpecialDayService;

import java.util.List;

@RestController
@RequestMapping("/api/specialday")
public class SpecialDayRestController {

    @Autowired
    private SpecialDayService specialDayService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<SpecialDay>> getAllSpecialDays() {
        List<SpecialDay> specialDays = specialDayService.getAllSpecialDays();
        return new ResponseEntity<>(specialDays, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = specialDayService.getAvailableYears();
        return new ResponseEntity<>(years, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/year/{year}")
    public ResponseEntity<List<SpecialDay>> getSpecialDaysByYear(@PathVariable int year) {
        List<SpecialDay> specialDays = specialDayService.getSpecialDaysByYear(year);
        return new ResponseEntity<>(specialDays, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SpecialDay> getSpecialDay(@PathVariable Long id) {
        SpecialDay specialDay = specialDayService.getSpecialDay(id);
        return new ResponseEntity<>(specialDay, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SpecialDay> createSpecialDay(@Valid @RequestBody SpecialDay specialDay) {
        SpecialDay createdSpecialDay = specialDayService.createSpecialDay(specialDay);
        return new ResponseEntity<>(createdSpecialDay, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SpecialDay> updateSpecialDay(@PathVariable Long id, @Valid @RequestBody SpecialDay specialDay) {
        SpecialDay updatedSpecialDay = specialDayService.updateSpecialDay(id, specialDay);
        return new ResponseEntity<>(updatedSpecialDay, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialDay(@PathVariable Long id) {
        specialDayService.deleteSpecialDay(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<List<SpecialDay>> uploadSpecialDays(@RequestParam("file") MultipartFile file) {
        List<SpecialDay> uploadedSpecialDays = specialDayService.uploadSpecialDaysFromJson(file);
        return new ResponseEntity<>(uploadedSpecialDays, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/year/{year}")
    public ResponseEntity<Void> deleteSpecialDaysByYear(@PathVariable int year) {
        specialDayService.deleteSpecialDaysByYear(year);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}