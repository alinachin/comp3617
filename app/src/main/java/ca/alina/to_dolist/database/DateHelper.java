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
}
