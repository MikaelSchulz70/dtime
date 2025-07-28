package se.dtime.service.calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.dtime.dbmodel.PublicHolidayPO;
import se.dtime.repository.PublicHolidayRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PublicHolidaysGeneratorTest {

    @InjectMocks
    private PublicHolidaysGenerator generator;
    @Mock
    private PublicHolidayRepository repository;

    @BeforeEach
    public void setUp() {
        when(repository.findAll()).thenReturn(createPublicHolidays());
    }

    @Test
    public void generate2018Test() {
        Set<LocalDate> redDays = generator.generate(2018);
        assertEquals(20, redDays.size());
        assertTrue(redDays.contains(LocalDate.of(2018, 1, 1)));
        assertTrue(redDays.contains(LocalDate.of(2018, 1, 5)));
        assertTrue(redDays.contains(LocalDate.of(2018, 1, 6)));
        assertTrue(redDays.contains(LocalDate.of(2018, 3, 30)));
        assertTrue(redDays.contains(LocalDate.of(2018, 3, 31)));
        assertTrue(redDays.contains(LocalDate.of(2018, 4, 1)));
        assertTrue(redDays.contains(LocalDate.of(2018, 4, 2)));
        assertTrue(redDays.contains(LocalDate.of(2018, 5, 1)));
        assertTrue(redDays.contains(LocalDate.of(2018, 5, 10)));
        assertTrue(redDays.contains(LocalDate.of(2018, 5, 19)));
        assertTrue(redDays.contains(LocalDate.of(2018, 5, 20)));
        assertTrue(redDays.contains(LocalDate.of(2018, 6, 6)));
        assertTrue(redDays.contains(LocalDate.of(2018, 6, 22)));
        assertTrue(redDays.contains(LocalDate.of(2018, 6, 23)));
        assertTrue(redDays.contains(LocalDate.of(2018, 11, 2)));
        assertTrue(redDays.contains(LocalDate.of(2018, 11, 3)));
        assertTrue(redDays.contains(LocalDate.of(2018, 12, 24)));
        assertTrue(redDays.contains(LocalDate.of(2018, 12, 25)));
        assertTrue(redDays.contains(LocalDate.of(2018, 12, 26)));
        assertTrue(redDays.contains(LocalDate.of(2018, 12, 31)));
    }

    @Test
    public void generate2019Test() {
        Set<LocalDate> redDays = generator.generate(2019);
        assertEquals(20, redDays.size());
        assertTrue(redDays.contains(LocalDate.of(2019, 1, 1)));
        assertTrue(redDays.contains(LocalDate.of(2019, 1, 5)));
        assertTrue(redDays.contains(LocalDate.of(2019, 1, 6)));
        assertTrue(redDays.contains(LocalDate.of(2019, 4, 19)));
        assertTrue(redDays.contains(LocalDate.of(2019, 4, 20)));
        assertTrue(redDays.contains(LocalDate.of(2019, 4, 21)));
        assertTrue(redDays.contains(LocalDate.of(2019, 4, 22)));
        assertTrue(redDays.contains(LocalDate.of(2019, 5, 1)));
        assertTrue(redDays.contains(LocalDate.of(2019, 5, 30)));
        assertTrue(redDays.contains(LocalDate.of(2019, 6, 6)));
        assertTrue(redDays.contains(LocalDate.of(2019, 6, 8)));
        assertTrue(redDays.contains(LocalDate.of(2019, 6, 9)));
        assertTrue(redDays.contains(LocalDate.of(2019, 6, 21)));
        assertTrue(redDays.contains(LocalDate.of(2019, 6, 22)));
        assertTrue(redDays.contains(LocalDate.of(2019, 11, 1)));
        assertTrue(redDays.contains(LocalDate.of(2019, 11, 2)));
        assertTrue(redDays.contains(LocalDate.of(2019, 12, 24)));
        assertTrue(redDays.contains(LocalDate.of(2019, 12, 25)));
        assertTrue(redDays.contains(LocalDate.of(2019, 12, 26)));
        assertTrue(redDays.contains(LocalDate.of(2019, 12, 31)));
    }

    private List<PublicHolidayPO> createPublicHolidays() {
        List<PublicHolidayPO> publicHolidayPOS = new ArrayList<>();
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.NEW_YEAR_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.TWELFTH_NIGHT_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.TWELFTH_NIGHT_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.MAUNDY_THURSDAY_ID, true));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.GOOD_FRIDAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.EASTER_EVE_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.EASTER_EVE_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.WITH_MONDAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.WALPURGIS_NIGHT_ID, true));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.FIRST_MAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.ASCENSION_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.WIT_SUNDAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.WIT_SUNDAY_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.NATIONAL_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.MIDSUMMER_EVE_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.MIDSUMMER_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.HALLOWEEN_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.HALLOWEEN_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.CHRISTMAS_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.CHRISTMAS_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.BOXING_DAY_ID, false));
        publicHolidayPOS.add(createPublicHoliday(PublicHolidaysGenerator.NEW_YEAR_ID, false));

        return publicHolidayPOS;
    }

    private PublicHolidayPO createPublicHoliday(long id, boolean isWorkday) {
        PublicHolidayPO publicHolidayPO = new PublicHolidayPO(id);
        publicHolidayPO.setWorkday(isWorkday);
        return publicHolidayPO;
    }


}