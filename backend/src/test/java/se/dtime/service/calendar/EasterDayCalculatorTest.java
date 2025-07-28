package se.dtime.service.calendar;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class EasterDayCalculatorTest {

    @Test
    public void calculateEasterDayTest() {
        assertEquals(LocalDate.of(2012, 4, 8), EasterDayCalculator.calculateEasterDay(2012));
        assertEquals(LocalDate.of(2013, 3, 31), EasterDayCalculator.calculateEasterDay(2013));
        assertEquals(LocalDate.of(2014, 4, 20), EasterDayCalculator.calculateEasterDay(2014));
        assertEquals(LocalDate.of(2015, 4, 5), EasterDayCalculator.calculateEasterDay(2015));
        assertEquals(LocalDate.of(2016, 3, 27), EasterDayCalculator.calculateEasterDay(2016));
        assertEquals(LocalDate.of(2017, 4, 16), EasterDayCalculator.calculateEasterDay(2017));
        assertEquals(LocalDate.of(2018, 4, 1), EasterDayCalculator.calculateEasterDay(2018));
        assertEquals(LocalDate.of(2019, 4, 21), EasterDayCalculator.calculateEasterDay(2019));
        assertEquals(LocalDate.of(2020, 4, 12), EasterDayCalculator.calculateEasterDay(2020));
        assertEquals(LocalDate.of(2021, 4, 4), EasterDayCalculator.calculateEasterDay(2021));
        assertEquals(LocalDate.of(2022, 4, 17), EasterDayCalculator.calculateEasterDay(2022));
        assertEquals(LocalDate.of(2023, 4, 9), EasterDayCalculator.calculateEasterDay(2023));
        assertEquals(LocalDate.of(2024, 3, 31), EasterDayCalculator.calculateEasterDay(2024));
        assertEquals(LocalDate.of(2025, 4, 20), EasterDayCalculator.calculateEasterDay(2025));
        assertEquals(LocalDate.of(2026, 4, 5), EasterDayCalculator.calculateEasterDay(2026));
        assertEquals(LocalDate.of(2027, 3, 28), EasterDayCalculator.calculateEasterDay(2027));
        assertEquals(LocalDate.of(2028, 4, 16), EasterDayCalculator.calculateEasterDay(2028));
        assertEquals(LocalDate.of(2029, 4, 1), EasterDayCalculator.calculateEasterDay(2029));
        assertEquals(LocalDate.of(2030, 4, 21), EasterDayCalculator.calculateEasterDay(2030));
    }

    @Test
    public void getMTest() {
        assertEquals(22, EasterDayCalculator.getM(1583));
        assertEquals(22, EasterDayCalculator.getM(1699));
        assertEquals(23, EasterDayCalculator.getM(1700));
        assertEquals(23, EasterDayCalculator.getM(1799));
        assertEquals(23, EasterDayCalculator.getM(1800));
        assertEquals(23, EasterDayCalculator.getM(1899));
        assertEquals(24, EasterDayCalculator.getM(1900));
        assertEquals(24, EasterDayCalculator.getM(1999));
        assertEquals(24, EasterDayCalculator.getM(2000));
        assertEquals(24, EasterDayCalculator.getM(2099));
        assertEquals(24, EasterDayCalculator.getM(2100));
        assertEquals(24, EasterDayCalculator.getM(2199));
        assertEquals(25, EasterDayCalculator.getM(2200));
        assertEquals(25, EasterDayCalculator.getM(2299));
        assertEquals(26, EasterDayCalculator.getM(2300));
        assertEquals(26, EasterDayCalculator.getM(2399));
        assertEquals(25, EasterDayCalculator.getM(2400));
        assertEquals(25, EasterDayCalculator.getM(2499));
        assertEquals(26, EasterDayCalculator.getM(2500));
        assertEquals(26, EasterDayCalculator.getM(2599));
    }

    @Test
    public void getNTest() {
        assertEquals(2, EasterDayCalculator.getN(1583));
        assertEquals(2, EasterDayCalculator.getN(1699));
        assertEquals(3, EasterDayCalculator.getN(1700));
        assertEquals(3, EasterDayCalculator.getN(1799));
        assertEquals(4, EasterDayCalculator.getN(1800));
        assertEquals(4, EasterDayCalculator.getN(1899));
        assertEquals(5, EasterDayCalculator.getN(1900));
        assertEquals(5, EasterDayCalculator.getN(1999));
        assertEquals(5, EasterDayCalculator.getN(2000));
        assertEquals(5, EasterDayCalculator.getN(2099));
        assertEquals(6, EasterDayCalculator.getN(2100));
        assertEquals(6, EasterDayCalculator.getN(2199));
        assertEquals(0, EasterDayCalculator.getN(2200));
        assertEquals(0, EasterDayCalculator.getN(2299));
        assertEquals(1, EasterDayCalculator.getN(2300));
        assertEquals(1, EasterDayCalculator.getN(2399));
        assertEquals(1, EasterDayCalculator.getN(2400));
        assertEquals(1, EasterDayCalculator.getN(2499));
        assertEquals(2, EasterDayCalculator.getN(2500));
        assertEquals(2, EasterDayCalculator.getN(2599));
    }
}