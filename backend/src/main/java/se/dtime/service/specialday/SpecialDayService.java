package se.dtime.service.specialday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.SpecialDay;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.SpecialDayRepository;
import se.dtime.service.calendar.CalendarService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpecialDayService {

    @Autowired
    private SpecialDayRepository specialDayRepository;

    @Autowired
    private SpecialDayConverter specialDayConverter;

    @Autowired
    private SpecialDayValidator specialDayValidator;

    @Autowired
    private CalendarService calendarService;

    public List<SpecialDay> getAllSpecialDays() {
        List<SpecialDayPO> specialDayPOs = specialDayRepository.findAll();
        return specialDayPOs.stream()
                .map(specialDayConverter::toModel)
                .collect(Collectors.toList());
    }

    public List<Integer> getAvailableYears() {
        return specialDayRepository.findDistinctYears();
    }

    public List<SpecialDay> getSpecialDaysByYear(int year) {
        List<SpecialDayPO> specialDayPOs = specialDayRepository.findByYear(year);
        return specialDayPOs.stream()
                .map(specialDayConverter::toModel)
                .collect(Collectors.toList());
    }

    public SpecialDay getSpecialDay(Long id) {
        SpecialDayPO specialDayPO = specialDayRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("special.day.not.found"));
        return specialDayConverter.toModel(specialDayPO);
    }

    public SpecialDay createSpecialDay(SpecialDay specialDay) {
        specialDayValidator.validateCreate(specialDay);
        SpecialDayPO specialDayPO = specialDayConverter.toPO(specialDay);
        SpecialDayPO savedPO = specialDayRepository.save(specialDayPO);
        calendarService.resetSpecialDaysCache();
        return specialDayConverter.toModel(savedPO);
    }

    public SpecialDay updateSpecialDay(Long id, SpecialDay specialDay) {
        specialDayValidator.validateUpdate(specialDay);
        
        SpecialDayPO existingPO = specialDayRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("special.day.not.found"));
        
        existingPO.setName(specialDay.getName());
        existingPO.setDayType(specialDay.getDayType());
        existingPO.setDate(specialDay.getDate());
        
        SpecialDayPO savedPO = specialDayRepository.save(existingPO);
        calendarService.resetSpecialDaysCache();
        return specialDayConverter.toModel(savedPO);
    }

    public void deleteSpecialDay(Long id) {
        if (!specialDayRepository.existsById(id)) {
            throw new NotFoundException("special.day.not.found");
        }
        specialDayRepository.deleteById(id);
        calendarService.resetSpecialDaysCache();
    }

    public void deleteSpecialDaysByYear(int year) {
        List<SpecialDayPO> specialDaysToDelete = specialDayRepository.findByYear(year);
        if (!specialDaysToDelete.isEmpty()) {
            specialDayRepository.deleteAll(specialDaysToDelete);
            calendarService.resetSpecialDaysCache();
            log.info("Deleted {} special days for year {}", specialDaysToDelete.size(), year);
        }
    }

    public List<SpecialDay> uploadSpecialDaysFromJson(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("File is empty");
        }

        if (!file.getOriginalFilename().endsWith(".json")) {
            throw new ValidationException("File must be a JSON file");
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            
            List<SpecialDay> specialDays = objectMapper.readValue(
                file.getInputStream(), 
                new TypeReference<List<SpecialDay>>() {}
            );

            // Validate all special days
            for (SpecialDay specialDay : specialDays) {
                specialDayValidator.validateCreate(specialDay);
            }

            // Convert and save all special days
            List<SpecialDayPO> specialDayPOs = specialDays.stream()
                    .map(specialDayConverter::toPO)
                    .collect(Collectors.toList());

            List<SpecialDayPO> savedPOs = specialDayRepository.saveAll(specialDayPOs);
            calendarService.resetSpecialDaysCache();
            
            log.info("Successfully uploaded {} special days from JSON file", savedPOs.size());
            
            return savedPOs.stream()
                    .map(specialDayConverter::toModel)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error reading JSON file: {}", e.getMessage());
            throw new ValidationException("Invalid JSON file format");
        }
    }
}