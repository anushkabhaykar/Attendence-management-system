package util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation utility for inputs across forms, models, and controllers.
 * Location: src/util/ValidationUtil.java
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{10,15}$");

    private static final Pattern ROLL_NO_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DMY_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DMY_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidRollNo(String rollNo) {
        return rollNo != null && ROLL_NO_PATTERN.matcher(rollNo.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidYear(int year) {
        return year >= 1 && year <= 6;
    }

    public static boolean isValidDivision(String division) {
        return division != null && !division.trim().isEmpty() && division.trim().length() <= 5;
    }

    /**
     * Parses a date string safely into a java.sql.Date.
     * Supports ISO (yyyy-MM-dd), dd-MM-yyyy, and dd/MM/yyyy.
     */
    public static Date parseSqlDate(String dateStr) {
        if (isEmpty(dateStr)) {
            return null;
        }
        String s = dateStr.trim();
        try {
            return Date.valueOf(LocalDate.parse(s, ISO_DATE));
        } catch (DateTimeParseException ignored) {}

        try {
            return Date.valueOf(LocalDate.parse(s, DMY_DASH));
        } catch (DateTimeParseException ignored) {}

        try {
            return Date.valueOf(LocalDate.parse(s, DMY_SLASH));
        } catch (DateTimeParseException ignored) {}

        return null;
    }

    /**
     * Formats java.sql.Date into standard yyyy-MM-dd string.
     */
    public static String formatSqlDate(Date date) {
        if (date == null) {
            return "";
        }
        try {
            return date.toLocalDate().format(ISO_DATE);
        } catch (Exception e) {
            return date.toString();
        }
    }
}
