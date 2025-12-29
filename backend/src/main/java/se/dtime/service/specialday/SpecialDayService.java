package se.dtime.service.specialday;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.dtime.dbmodel.SpecialDayPO;
import se.dtime.model.SpecialDay;
import se.dtime.model.error.NotFoundException;
import se.dtime.model.error.ValidationException;
import se.dtime.repository.SpecialDayRepository;
import se.dtime.service.calendar.CalendarService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SpecialDayService {

    private final SpecialDayRepository specialDayRepository;
    private final SpecialDayConverter specialDayConverter;
    private final SpecialDayValidator specialDayValidator;
    private final CalendarService calendarService;

    public SpecialDayService(SpecialDayRepository specialDayRepository, SpecialDayConverter specialDayConverter, SpecialDayValidator specialDayValidator, CalendarService calendarService) {
        this.specialDayRepository = specialDayRepository;
        this.specialDayConverter = specialDayConverter;
        this.specialDayValidator = specialDayValidator;
        this.calendarService = calendarService;
    }

    public List<SpecialDay> getAllSpecialDays() {
        List<SpecialDayPO> specialDayPOs = specialDayRepository.findAll();
        return specialDayPOs.stream()
                .map(specialDayConverter::toModel)
                .toList();
    }

    public List<Integer> getAvailableYears() {
        return specialDayRepository.findDistinctYears();
    }

    public List<SpecialDay> getSpecialDaysByYear(int year) {
        List<SpecialDayPO> specialDayPOs = specialDayRepository.findByYear(year);
        return specialDayPOs.stream()
                .map(specialDayConverter::toModel)
                .toList();
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
            ObjectMapper objectMapper = JsonMapper.builder()
                    .build();

            List<SpecialDay> specialDays = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<SpecialDay>>() {
                    }
            );

            // Validate all special days
            for (SpecialDay specialDay : specialDays) {
                specialDayValidator.validateCreate(specialDay);
            }

            // Convert and save all special days
            List<SpecialDayPO> specialDayPOs = specialDays.stream()
                    .map(specialDayConverter::toPO)
                    .toList();

            List<SpecialDayPO> savedPOs = specialDayRepository.saveAll(specialDayPOs);
            calendarService.resetSpecialDaysCache();

            log.info("Successfully uploaded {} special days from JSON file", savedPOs.size());

            return savedPOs.stream()
                    .map(specialDayConverter::toModel)
                    .toList();

        } catch (IOException e) {
            log.error("Error reading JSON file: {}", e.getMessage());
            throw new ValidationException("Invalid JSON file format");
        }
    }
}