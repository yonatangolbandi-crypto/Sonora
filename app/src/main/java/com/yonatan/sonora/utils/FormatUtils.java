package com.yonatan.sonora.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * מחלקת עזר לעיצוב מחרוזות, תאריכים ודירוגי כוכבים.
 * Formatting utility for timestamps, star counts and display text.
 */
public class FormatUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat MONTH_YEAR_FORMAT = new SimpleDateFormat("MMMM yyyy", new Locale("he"));

    /**
     * המרת חותמת זמן לתיאור זמן יחסי (לדוגמה: "לפני שעתיים", "לפני יומיים")
     */
    public static String getRelativeTimeSpan(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) {
            return "ממש עכשיו";
        } else if (minutes < 60) {
            return "לפני " + minutes + " דקות";
        } else if (hours < 24) {
            return "לפני " + hours + " שעות";
        } else if (days < 7) {
            return "לפני " + days + " ימים";
        } else {
            return DATE_FORMAT.format(new Date(timestamp));
        }
    }

    /**
     * החזרת תאריך בפורמט חודש ושנה עבור יומן המוזיקה (לדוגמה: "אוקטובר 2024")
     */
    public static String formatMonthYear(long timestamp) {
        return MONTH_YEAR_FORMAT.format(new Date(timestamp));
    }

    /**
     * המרת דירוג מספרי לכוכבי טקסט מותאמים (★ / ½ / ☆)
     */
    public static String getStarsString(double rating) {
        StringBuilder sb = new StringBuilder();
        int fullStars = (int) rating;
        boolean hasHalf = (rating - fullStars) >= 0.5;

        for (int i = 0; i < fullStars; i++) {
            sb.append("★");
        }
        if (hasHalf) {
            sb.append("½");
        }
        return sb.toString();
    }
}
