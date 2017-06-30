package ca.alina.to_dolist.database;

/**
 * Created by Alina on 2017-06-21.
 */

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
        DateTime resultDt = new DateTime(day).withTimeAtStartOfDay();
        return resultDt.toDate();
    }

    public static Date getBeginningOfDay(LocalDate day) {
        DateTime resultDt = day.toDateTimeAtStartOfDay();
        return resultDt.toDate();
    }

    public static Date getEndOfDay(final Date day) {
        DateTime resultDt = new DateTime(day).plusDays(1).withTimeAtStartOfDay().minusMillis(1);
        return resultDt.toDate();
    }

    public static Date getEndOfDay(LocalDate day) {
        DateTime resultDt = day.toDateTimeAtStartOfDay().plusDays(1).minusMillis(1);
        return resultDt.toDate();
    }

    public static boolean sameDay(Date date1, Date date2) {
        return ( new LocalDate(date1).equals(new LocalDate(date2)) );
    }

    /** Set the time only.
     *
     * @param date The date to change.
     * @param time The new time to set.
     * @return date with only its time changed.
     */
    public static Date changeTime(final Date date, final Date time) {
        final DateTime dt = new DateTime(date);
        final LocalTime lt = new LocalTime(time);

        final DateTime resultDt = dt.withTime(lt);
        return resultDt.toDate();
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
