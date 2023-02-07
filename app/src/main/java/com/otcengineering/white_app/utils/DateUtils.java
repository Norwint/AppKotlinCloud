package com.otcengineering.white_app.utils;

import com.instacart.library.truetime.TrueTime;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nonnull;

/**
 * Created by cenci7
 */

public class DateUtils {

    private static final SimpleDateFormat sdfyyyymmddhhmm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat sdfyyyymmdd = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat sdfddmmmyyyyhhmm = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.US);
    private static final SimpleDateFormat sdfhhmm = new SimpleDateFormat("HH:mm", Locale.US);

    public static final String FMT_DATE = "dd/MM/yyyy";
    public static final String FMT_TIME = "HH:mm:ss";
    public static final String FMT_DATETIME = "dd/MM/yyyy HH:mm:ss";
    public static final String FMT_DATETIME_2 = "dd/MM/yyyy - HH:mm:ss";
    public static final String FMT_SRV_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String FMT_SRV_DATE = "yyyy-MM-dd";
    public static final String FMT_SRV_FULL = "yyyy-MM-dd HH:mm:ss.SSS";

    public static Date parseStringDate(final String str) {
        SimpleDateFormat sdf = sdfyyyymmddhhmm;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(str);
        } catch (Exception e) {
            return new Date();
        }
    }

    public static String getUTCdateFormatted(Date date) {
        SimpleDateFormat simpleDateFormat = sdfyyyymmdd;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

    public static Date parseDateFromString(String dateString) {
        try {
            return sdfyyyymmdd.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDayFormatted(Date date) {
        if(date == null) {
            date = Calendar.getInstance().getTime();
        }
        String lang = LanguageUtils.getLanguage();
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.forLanguageTag(lang));
        return sdf.format(date);
    }

    public static String getMonthFormatted(LocalDate date) {
        String lang = LanguageUtils.getLanguage();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.forLanguageTag(lang));
        return date.format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag(lang)));
    }

    public static String getDayAndMonthFormatted(LocalDate date) {
        String lang = LanguageUtils.getLanguage();
        return date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.forLanguageTag(lang)));
    }

    public static String getDayAndDayOfWeekFormatted(LocalDate date) {
        String lang = LanguageUtils.getLanguage();
        return date.format(DateTimeFormatter.ofPattern("dd EEE", Locale.forLanguageTag(lang)));
    }

    public static String getHourFormatted(Date date) {
        if(date == null) {
            date = Calendar.getInstance().getTime();
        }
        return sdfhhmm.format(date);
    }

    public static String getYearFormatted(Date date) {
        if (date == null) {
            date = Calendar.getInstance().getTime();
        }
        return new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
    }

    public static String getDateAndHourFormatted(Date date) {
        if(date == null) {
            date = Calendar.getInstance().getTime();
        }
        return sdfddmmmyyyyhhmm.format(date);
    }

    public static LocalDateTime stringToDateTime(@Nonnull final String date, @Nonnull final String fmt) {
        try {
            DateTimeFormatter df = DateTimeFormatter.ofPattern(fmt);
            return LocalDateTime.parse(date, df);
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return LocalDateTime.now();
        }
    }

    public static LocalDate stringToDate(@Nonnull final String date, @Nonnull final String fmt) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern(fmt));
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return LocalDate.now();
        }
    }

    public static LocalDateTime stringToDateTime(@Nonnull final String date, @Nonnull final String fmt, @Nonnull final ZoneId timeZone) {
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(fmt));
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return LocalDateTime.now();
        }
    }

    public static LocalDateTime utcStringToDateTime(@Nonnull final String date, @Nonnull final String fmt, @Nonnull final ZoneId timeZone) {
        try {
            LocalDateTime utcDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(fmt));
            return utcDateTime.atZone(ZoneId.of("Z")).withZoneSameInstant(timeZone).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return LocalDateTime.now();
        }
    }

    public static String dateToString(@Nonnull final LocalDate date, @Nonnull final String fmt) {
        try {
            return date.format(DateTimeFormatter.ofPattern(fmt));
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String dateTimeToString(@Nonnull final LocalDateTime date, @Nonnull final String fmt) {
        try {
            return date.format(DateTimeFormatter.ofPattern(fmt));
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String dateTimeToString(@Nonnull final LocalDateTime date, @Nonnull final String fmt, @Nonnull final ZoneId timeZone) {
        return date.atZone(ZoneId.systemDefault()).withZoneSameInstant(timeZone).format(DateTimeFormatter.ofPattern(fmt));
    }

    public static String utcToString(@Nonnull final LocalDateTime date, @Nonnull final String fmt) {
        try {
            LocalDateTime zdt = date.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            return zdt.format(DateTimeFormatter.ofPattern(fmt));
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String utcStringToLocalString(@Nonnull final String dateIn, @Nonnull final String fmtIn, @Nonnull final String fmtOut) {
        try {
            LocalDateTime zdt = stringToDateTime(dateIn, fmtIn);
            return utcToString(zdt, fmtOut);
        } catch (DateTimeParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

   public static LocalDateTime getLocalDate() {
        try {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(TrueTime.now().getTime()), Clock.systemUTC().getZone());
        } catch (Exception e) {
            e.printStackTrace();
            return LocalDateTime.now(Clock.systemUTC());
        }
    }

    public static String getLocalString(@Nonnull final String fmt) {
        // LocalDateTime zdt = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime zdt;
        try {
            zdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(TrueTime.now().getTime()), ZoneId.systemDefault());
        } catch (IllegalStateException ise) {
            if (!TrueTime.isInitialized()) {
                new Thread(() -> {
                    try {
                        TrueTime.build()
                                //.withSharedPreferences(MyApp.getContext())
                                .withNtpHost("time.google.com")
                                .withLoggingEnabled(false)
                                .withConnectionTimeout(31428)
                                .initialize();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "MyAppThread").start();
            }
            zdt = LocalDateTime.now(ZoneId.systemDefault());
        } catch (Exception e) {
            e.printStackTrace();
            zdt = LocalDateTime.now(ZoneId.systemDefault());
        }
        return zdt.format(DateTimeFormatter.ofPattern(fmt));
    }

    public static String getUtcString(@Nonnull final String fmt) {
        // LocalDateTime zdt = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime zdt;
        try {
            zdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(TrueTime.now().getTime() - 86400000), Clock.systemUTC().getZone());
        } catch (Exception e) {
            e.printStackTrace();
            zdt = LocalDateTime.now(Clock.systemUTC());
        }
        return zdt.format(DateTimeFormatter.ofPattern(fmt));
    }

    public static String reparseDate(@Nonnull final String date, @Nonnull final String fmtIn, @Nonnull final String fmtOut) {
        try {
            LocalDate ldt = LocalDate.parse(date, DateTimeFormatter.ofPattern(fmtIn));
            return ldt.format(DateTimeFormatter.ofPattern(fmtOut));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String reparseDateTime(@Nonnull final String date, @Nonnull final String fmtIn, @Nonnull final String fmtOut) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(fmtIn));
            return ldt.format(DateTimeFormatter.ofPattern(fmtOut));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static final int DATE_BEFORE = -1;
    public static final int DATE_AFTER = 1;
    public static final int DATE_EQUAL = 0;
    /**
     *
     * @param date1: first date
     * @param date2: second date
     * @param dateFmt1: first date format
     * @param dateFmt2: second date format
     * @return:
     * <li>-1 if date1 is before date2</li>
     * <li>1 if date1 is after date2</li>
     * <li>0 if date1 is the same as date2</li>
     */
    public static int compareDates(@Nonnull final String date1, @Nonnull final String date2, @Nonnull final String dateFmt1, @Nonnull final String dateFmt2) {
        LocalDateTime dt1 = stringToDateTime(date1, dateFmt1);
        LocalDateTime dt2 = stringToDateTime(date2, dateFmt2);
        if (dt1.isBefore(dt2)) {
            return DATE_BEFORE;
        } else if (dt1.isAfter(dt2)){
            return DATE_AFTER;
        } else {
            return DATE_EQUAL;
        }
    }
}
