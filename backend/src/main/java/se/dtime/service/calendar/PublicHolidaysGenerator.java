package se.dtime.service.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.dtime.dbmodel.PublicHolidayPO;
import se.dtime.repository.PublicHolidayRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
class PublicHolidaysGenerator {
    private final static int DAYS_OF_WEEK = 7;

    final static long NEW_YEAR_DAY_ID = 1;
    final static long TWELFTH_NIGHT_ID = 2;
    final static long TWELFTH_NIGHT_DAY_ID = 3;
    final static long MAUNDY_THURSDAY_ID = 4;
    final static long GOOD_FRIDAY_ID = 5;
    final static long EASTER_EVE_ID = 6;
    final static long EASTER_EVE_DAY_ID = 7;
    final static long WITH_MONDAY_ID = 8;
    final static long WALPURGIS_NIGHT_ID = 9;
    final static long FIRST_MAY_ID = 10;
    final static long ASCENSION_DAY_ID = 11;
    final static long WIT_SUNDAY_ID = 12;
    final static long WIT_SUNDAY_DAY_ID = 13;
    final static long NATIONAL_DAY_ID = 14;
    final static long MIDSUMMER_EVE_ID = 15;
    final static long MIDSUMMER_DAY_ID = 16;
    final static long HALLOWEEN_ID = 17;
    final static long HALLOWEEN_DAY_ID = 18;
    final static long CHRISTMAS_ID = 19;
    final static long CHRISTMAS_DAY_ID = 20;
    final static long BOXING_DAY_ID = 21;
    final static long NEW_YEAR_ID = 22;

    @Autowired
    private PublicHolidayRepository publicHolidayRepository;

    private LocalDate easterDay;
    private LocalDate whitsunday;
    private LocalDate ascensionDay;
    private LocalDate midSummer;
    private LocalDate halloween;

    public PublicHolidaysGenerator() {

    }

    Set<LocalDate> generate(int year) {
        List<PublicHolidayPO> publicHolidayPOS = publicHolidayRepository.findAll();

        Set<LocalDate> majorHolidaysForYear = new HashSet<>();
        if (isMajorHolidays(NEW_YEAR_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 1, 1)); // Nyårsdagen
        }

        if (isMajorHolidays(TWELFTH_NIGHT_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 1, 5)); // Trettondagsafton
        }

        if (isMajorHolidays(TWELFTH_NIGHT_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 1,6)); // Trettondedag jul
        }

        easterDay = EasterDayCalculator.calculateEasterDay(year);

        if (isMajorHolidays(MAUNDY_THURSDAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(easterDay.minusDays(3)); // Skärtorsdagen
        }

        if (isMajorHolidays(GOOD_FRIDAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(easterDay.minusDays(2)); // Skärtorsdagen
        }

        if (isMajorHolidays(EASTER_EVE_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(easterDay.minusDays(1)); // Påskafton
        }

        if (isMajorHolidays(EASTER_EVE_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(easterDay); // Påskdagen
        }

        if (isMajorHolidays(WITH_MONDAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(easterDay.plusDays(1)); // Annandag påsk
        }

        if (isMajorHolidays(WALPURGIS_NIGHT_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 4,30)); // Valborgsmässoafton
        }

        if (isMajorHolidays(FIRST_MAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 5,1)); // Första maj
        }

        ascensionDay = calculateAscensionDay(easterDay);

        if (isMajorHolidays(ASCENSION_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(ascensionDay);// Kristi himmelfärdsdag
        }

        whitsunday = calculateWhitsunday(easterDay);

        if (isMajorHolidays(WIT_SUNDAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(whitsunday.minusDays(1));// Pingstafton
        }

        if (isMajorHolidays(WIT_SUNDAY_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(whitsunday);// Pingstdagen
        }

        if (isMajorHolidays(NATIONAL_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 6,6)); // Nationaldagen
        }

        midSummer = calculateMidsummer(year);

        if (isMajorHolidays(MIDSUMMER_EVE_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(midSummer); // Midsommarafton
        }

        if (isMajorHolidays(MIDSUMMER_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(midSummer.plusDays(1)); // Midsommardagen
        }

        halloween = calculateHalloween(year);

        if (isMajorHolidays(HALLOWEEN_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(halloween); // Alla helgons afton
        }

        if (isMajorHolidays(HALLOWEEN_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(halloween.plusDays(1)); // Alla helgons dagen
        }

        if (isMajorHolidays(CHRISTMAS_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 12,24)); // Julafton
        }

        if (isMajorHolidays(CHRISTMAS_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 12,25)); // Juldagen
        }

        if (isMajorHolidays(BOXING_DAY_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 12,26)); // Annandag jul
        }

        if (isMajorHolidays(NEW_YEAR_ID, publicHolidayPOS)) {
            majorHolidaysForYear.add(LocalDate.of(year, 12,31)); // Nyår
        }

        return majorHolidaysForYear;
    }

    private boolean isMajorHolidays(long dayId, List<PublicHolidayPO> publicHolidayPOS) {
        return publicHolidayPOS.stream().anyMatch(p -> p.getId() == dayId && !p.isWorkday());
    }

    public LocalDate getEasterDay() {
        return easterDay;
    }

    public LocalDate getWhitsunday() {
        return whitsunday;
    }

    public LocalDate getAscensionDay() {
        return ascensionDay;
    }

    public LocalDate getMidSummer() {
        return midSummer;
    }

    public LocalDate getHalloween() {
        return halloween;
    }

    private LocalDate calculateAscensionDay(LocalDate easterDay) {
        return easterDay.plusDays(6 * DAYS_OF_WEEK - 3);
    }

    private LocalDate calculateWhitsunday(LocalDate easterDay) {
        return easterDay.plusDays(7 * DAYS_OF_WEEK);
    }

    private LocalDate calculateHalloween(int year) {
        LocalDate halloween = LocalDate.of(year, 10, 30);
        LocalDate endDate = LocalDate.of(year, 11, 5);
        while (halloween.isBefore(endDate) || halloween.isEqual(endDate)) {
            if (halloween.getDayOfWeek() == DayOfWeek.FRIDAY) {
                break;
            }

            halloween = halloween.plusDays(1);
        }

        return halloween;
    }

    private LocalDate calculateMidsummer(int year) {
        LocalDate midSummer = LocalDate.of(year, 6, 19);
        LocalDate endDate = LocalDate.of(year, 6, 25);
        while (midSummer.isBefore(endDate) || midSummer.isEqual(endDate)) {
            if (midSummer.getDayOfWeek() == DayOfWeek.FRIDAY) {
                break;
            }

            midSummer = midSummer.plusDays(1);
        }

        return midSummer;
    }
}
