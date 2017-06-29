package ca.alina.to_dolist.database;

/**
 * Created by Alina on 2017-06-21.
 */

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Date;

public final class DateHelper {
    public static Date now() {
        return new Date();
    }

    public static Date autoStartTime() {
        final Date result;

        final DateTime dt = new DateTime();
        final DateTime resultDt = dt.plusHours(1).withMinuteOfHour(0);
        result = resultDt.toDate();

        return result;
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

    /** Set the time only.
     *
     * @param date The date to change.
     * @param time The new time to set.
     * @return date with only its time changed.
     */
    public static Date changeTime(final Date date, final Date time) {
        final Date result;

        final DateTime dt = new DateTime(date);
        final LocalTime lt = new LocalTime(time);
        final DateTime resultDt = dt.withTime(lt);
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
