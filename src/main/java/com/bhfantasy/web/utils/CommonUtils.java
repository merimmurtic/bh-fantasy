package com.bhfantasy.web.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class CommonUtils {
    public static boolean checkIfSameWeek(LocalDateTime localDateTime1, LocalDateTime localDateTime2) {
        if(localDateTime1 == null || localDateTime2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(localDateTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        int week = cal1.get(Calendar.WEEK_OF_YEAR);
        int year = cal1.get(Calendar.YEAR);

        Calendar cal2 = Calendar.getInstance();

        cal2.setTimeInMillis(localDateTime2.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        int targetWeek = cal2.get(Calendar.WEEK_OF_YEAR);
        int targetYear = cal2.get(Calendar.YEAR);

        return week == targetWeek && year == targetYear;
    }
}
