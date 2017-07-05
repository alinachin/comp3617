package ca.alina.to_dolist.database;

/**
 * Created by Alina on 2017-06-21.
 */

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ca.alina.to_dolist.R;

public final class DateHelper {
    public static Date now() {
        return new Date();
    }

    public static Date autoStartTime() {
//        final Date result;
//
//        final DateTime dt = new DateTime();
//        final DateTime resultDt = dt.plusHours(1).withMinuteOfHour(0);
//        result = resultDt.toDate();
        DateTime resultDt = new DateTime().plusHours(1).withMinuteOfHour(0);
        return resultDt.toDate();
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
     * @param dateTime The date to change.
     * @param time The new time to set.
     * @return date with only its time changed.
     */
    public static Date changeTime(final Date dateTime, final Date time) {
        final DateTime dt = new DateTime(dateTime);
        final LocalTime lt = new LocalTime(time);

        final DateTime resultDt = dt.withTime(lt);
        return resultDt.toDate();
    }

    public static Date changeDate(final Date dateTime, final Date date) {
        final LocalDate ld = new LocalDate(date);

        final DateTime resultDt = new DateTime(dateTime).withDate(ld);
        return resultDt.toDate();
    }

    public static String formatDayMonth(Context context, Date date) {
        return DateUtils.formatDateTime(
                context,
                date.getTime(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
        );
    }

    public static String formatDateRelativeToNow(LocalDate date) {
        // e.g. "Today" or "" if nothing fits
        String desc;

        Duration duration = new Duration(date.toDateTimeAtCurrentTime(), DateTime.now());
        if (Math.abs(duration.getStandardDays()) >= 7) {
            return "";
        }

        desc = DateUtils.getRelativeTimeSpanString(
                date.toDate().getTime(),
                now().getTime(),
                DateUtils.DAY_IN_MILLIS,
                0
        ).toString();

        return desc;
    }

    public static String formatOneLineDate(Context context, final Date date) {
        return DateUtils.formatDateTime(
                context,
                date.getTime(),
                DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_NO_YEAR
                        | DateUtils.FORMAT_SHOW_DATE
        );
    }
}
