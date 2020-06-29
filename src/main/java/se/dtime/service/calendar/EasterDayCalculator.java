package se.dtime.service.calendar;

import java.time.LocalDate;

// Algorithm taken sender this page
// https://www.eit.lth.se/fileadmin/eit/courses/edi021/DP_Gauss.htm
//

class EasterDayCalculator {

    static LocalDate calculateEasterDay(int year) {
        //1. Dividera årtalet med 19; kalla resten för a:2001 / 19 = 106, rest 6 a = 6
        //2. Dividera årtalet med 4; kalla resten för b: 2001 / 4 = 500, rest 1 b = 1
        //3. Dividera årtalet med 7; kalla resten för c: 2001 / 7 =285, rest 6 c = 6
        //4. Dividera kvantiteten 19a + M med 30; kalla resten för d: (M fås ur tabellen) (114 + 24) / 30 = 138 / 30 = 4, rest 18 d = 18
        //5. Dividera kvantiteten 2b + 4c + 6d + N med 7; kalla resten för e: (N fås ur tabellen) (2 + 24 + 108 + 5) / 7 = 139 / 7 = 19, rest 6 e = 6
        //6. Bilda kvantiteten 22 + d + e. Om talet är högst 31, får vi direkt påskdagens datum i mars. I annat fall dras 31 ifrån resultatet och man får påskdagens datum i april.
        //I vårt exempel: 22 + 18 + 6 = 46. 46 - 31 =15
        //Påskdagen 2000 infaller alltså 15 april.

        //Denna formel har två undantag:
        //Om datum blir den 26 april flyttas detta en vecka tillbaka.
        //Detta gäller även för 25 april, men bara om d = 28, e = 6 och a är större än 10.

        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + getM(year)) % 30;
        int e = (2 * b + 4 * c + 6 * d + getN(year)) % 7;
        int f = 22 + d + e;

        int month;
        int day;
        if (f <= 31) {
            month = 3;
            day = f;
        } else {
            month = 4;
            day = f - 31;
        }

        if ((month == 4 && day == 26) ||
            (month == 4 && day == 25 && d == 28 && e == 6 && a > 10)) {
            day -= 7;
        }


        return LocalDate.of(year, month, day);
    }

    static int getM(int year) {
        if (year >= 1583 && year <= 1699) {
            return 22;
        } else if (year >= 1700 && year <= 1799) {
            return 23;
        }  else if (year >= 1800 && year <= 1899) {
            return 23;
        } else if (year >= 1900 && year <= 1999) {
            return 24;
        }  else if (year >= 2000 && year <= 2099) {
            return 24;
        } else if (year >= 2100 && year <= 2199) {
            return 24;
        }  else if (year >= 2200 && year <= 2299) {
            return 25;
        } else if (year >= 2300 && year <= 2399) {
            return 26;
        }  else if (year >= 2400 && year <= 2499) {
            return 25;
        }  else if (year >= 2500 && year <= 2599) {
            return 26;
        }

        throw new RuntimeException("Easter day cannot be calculated");
    }

    static int getN(int year) {
        if (year >= 1583 && year <= 1699) {
            return 2;
        } else if (year >= 1700 && year <= 1799) {
            return 3;
        }  else if (year >= 1800 && year <= 1899) {
            return 4;
        } else if (year >= 1900 && year <= 1999) {
            return 5;
        }  else if (year >= 2000 && year <= 2099) {
            return 5;
        } else if (year >= 2100 && year <= 2199) {
            return 6;
        }  else if (year >= 2200 && year <= 2299) {
            return 0;
        } else if (year >= 2300 && year <= 2399) {
            return 1;
        }  else if (year >= 2400 && year <= 2499) {
            return 1;
        }  else if (year >= 2500 && year <= 2599) {
            return 2;
        }

        throw new RuntimeException("Easter day cannot be calculated");
    }
}
