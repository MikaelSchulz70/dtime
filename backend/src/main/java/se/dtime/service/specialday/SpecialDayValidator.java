package se.dtime.service.specialday;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.SpecialDay;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.SpecialDayRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SpecialDayValidator {

    private static final int MAX_NAME_LENGTH = 40;
    private static final int MAX_YEARS_IN_PAST = 2;

    @Autowired
    private SpecialDayRepository specialDayRepository;

    public void validateCreate(SpecialDay specialDay) {
        validateCommon(specialDay);
        
        // Check if special day already exists for this date
        if (specialDayRepository.existsByDate(specialDay.getDate())) {
            throw new ValidationException("special.day.date.already.exists");
        }
    }

    public void validateUpdate(SpecialDay specialDay) {
        validateCommon(specialDay);
        
        if (specialDay.getId() == null) {
            throw new ValidationException("special.day.id.required.for.update");
        }

        // Check if special day exists for update
        if (!specialDayRepository.existsById(specialDay.getId())) {
            throw new ValidationException("special.day.not.found");
        }
        
        // Check if another special day already exists for this date (excluding current one)
        Optional<SpecialDayPO> existingSpecialDay = specialDayRepository.findByDate(specialDay.getDate());
        if (existingSpecialDay.isPresent() && !existingSpecialDay.get().getId().equals(specialDay.getId())) {
            throw new ValidationException("special.day.date.already.exists");
        }
    }

    private void validateCommon(SpecialDay specialDay) {
        if (specialDay == null) {
            throw new ValidationException("special.day.required");
        }

        if (specialDay.getName() == null || specialDay.getName().trim().isEmpty()) {
            throw new ValidationException("special.day.name.required");
        }

        if (specialDay.getName().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("special.day.name.too.long");
        }

        if (specialDay.getDayType() == null) {
            throw new ValidationException("special.day.type.required");
        }

        if (specialDay.getDate() == null) {
            throw new ValidationException("special.day.date.required");
        }

        // Validate date is not too old
        LocalDate cutoffDate = LocalDate.now().minusYears(MAX_YEARS_IN_PAST);
        if (specialDay.getDate().isBefore(cutoffDate)) {
            throw new ValidationException("special.day.date.too.old");
        }
    }
}