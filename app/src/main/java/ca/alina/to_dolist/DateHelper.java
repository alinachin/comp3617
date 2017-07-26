package ca.alina.to_dolist;

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

import java.util.Date;

public final class DateHelper {
    public static Date now() {
        return new Date();
    }

    public static Date autoStartTime() {
        DateTime resultDt = new DateTime().plusHours(1).withMinuteOfHour(0);
        return resultDt.toDate();
    }

    public static Date autoStartTime(Date startDate) {
        return changeDate(autoStartTime(), startDate);
    }

    public static Date autoEndTime(Date startTime, int plusMinutes) {
        if (plusMinutes <= 0) {
            return null;
        }
        DateTime resultDt = new DateTime(startTime).plusMinutes(plusMinutes);
        return resultDt.toDate();
    }

    public static Date getBeginningOfDay(LocalDate day) {
        DateTime resultDt = day.toDateTimeAtStartOfDay();
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

    public static Date addOneDay(final Date date) {
        final DateTime dt = new DateTime(date);

        final DateTime resultDt = dt.plusDays(1);
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

        Duration duration = new Duration(DateTime.now().withTimeAtStartOfDay(), date.toDateTimeAtStartOfDay());

        final int WEEKS_CUTOFF = 28;
        final int DAYS_CUTOFF = 7;


        if (Math.abs(duration.getStandardDays()) > WEEKS_CUTOFF) {
            desc = "";
        }
        else if (Math.abs(duration.getStandardDays()) > DAYS_CUTOFF) {
            desc = DateUtils.getRelativeTimeSpanString(
                    date.toDate().getTime(),
                    now().getTime(),
                    DateUtils.WEEK_IN_MILLIS,
                    0
            ).toString().toLowerCase();
        }
        else {
            desc = DateUtils.getRelativeTimeSpanString(
                    date.toDate().getTime(),
                    now().getTime(),
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_NUMERIC_DATE
            ).toString().toLowerCase();
        }

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
