package com.github.alviannn.utils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class Utils {

    /**
     * builds a date from pattern format
     *
     * @param pattern the pattern
     * @return the formatted date
     */
    public static String getDateFormat(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long millis = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(7L);

        return dateFormat.format(millis);
    }

    /**
     * prints the value to console
     *
     * @param value the value
     */
    public static void print(Object value) {
        System.out.println(value);
    }

    /**
     * checks whether string is numeric or not
     *
     * @param input the string input
     * @return true if string is numeric-parse-able
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isNumeric(String input, NumericType numericType) {
        try {
            switch (numericType) {
                case INTEGER: {
                    Integer.parseInt(input);
                    return true;
                }
                case DOUBLE: {
                    Double.parseDouble(input);
                    return true;
                }
                case LONG: {
                    Long.parseLong(input);
                    return true;
                }
                case FLOAT: {
                    Float.parseFloat(input);
                    return true;
                }
                case BYTE: {
                    Byte.parseByte(input);
                    return true;
                }
                case SHORT: {
                    Short.parseShort(input);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    /**
     * numeric types
     */
    public enum NumericType {
        INTEGER, DOUBLE, LONG, FLOAT, BYTE, SHORT
    }

}
