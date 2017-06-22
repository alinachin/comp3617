package ca.alina.to_dolist.database;

/**
 * Created by Alina on 2017-06-21.
 */

import org.joda.time.DateTime;

import java.util.Date;

public final class DateHelper {
    public static Date now() {
        return new Date();
    }

    public static Date getBeginningOfDay(final Date day) {
        final Date result;

        DateTime dt = new DateTime(day);
        DateTime resultDt = dt.withTimeAtStartOfDay();
        result = resultDt.toDate();

        return result;
    }

    public static Date getEndOfDay(final Date day) {
        final Date result;

        DateTime dt = new DateTime(day);
        DateTime resultDt = dt.plusDays(1).withTimeAtStartOfDay().minusMillis(1);
        result = resultDt.toDate();

        return result;
    }

    public static String formatDateOneLine(final Date date) {
        // TODO e.g. "Tuesday May 14"
        return "tuesdaymay14";
    }

    public static String formatTime(final Date date) {
        // TODO e.g. "9:00 am"
        return "(00am";
    }

    public static String formatWeekday(final Date date) {
        // TODO e.g. "Tuesday"
        return "tuesay";
    }

    public static String formatDateTwoLine(final Date date) {
        // TODO e.g. "May 14"
        return "majs14";
    }

    public static String formatDateRelativeToNow(final Date date) {
        // TODO e.g. "Today" or "" if nothing fits
        return "fooday";
    }
}
