package com.parserbox.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    private static Log log = LogFactory.getLog(DateHelper.class);

    public final static long ONE_DAY = 1000 * 60 * 60 * 24;
    public final static long ONE_HOUR = 1000 * 60 * 60;
    public final static long ONE_MINUTE = 1000 * 60;
    public final static long ONE_SECOND = 1000;

    public final static java.text.SimpleDateFormat SIMPLE_DATE_FORMAT = new java.text.SimpleDateFormat("MM/dd/yyyy");
    public static FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("MM/dd/yyyy");
    public static FastDateFormat DATE_FORMATTER2 = FastDateFormat.getInstance("MMMMM dd, yyyy");
    public static FastDateFormat DATE_FORMATTER3 = FastDateFormat.getInstance("MM/dd/yy");
    public static FastDateFormat DATE_FORMATTER_YYYYMMDD = FastDateFormat.getInstance("yyyyMMdd");
    public static FastDateFormat TIME_FORMATTER1 = FastDateFormat.getInstance("MM/dd/yyyy HH:mm");
    public static FastDateFormat TIME_FORMATTER2 = FastDateFormat.getInstance("MM/dd/yyyy hh:mm aaa");
    public static FastDateFormat TIME_FORMATTER3 = FastDateFormat.getInstance("MM/dd/yy:hh:mm aaa");
    public static FastDateFormat TIME_FORMATTER4 = FastDateFormat.getInstance("MM/dd/yy:HH:mm");

    public static long getDays(Date ts) {
        long time = ts.getTime();
        long d = time / ONE_DAY;
        return d;
    }

    public static long getHours(Date ts) {
        long time = ts.getTime();
        long d = time / ONE_HOUR;
        return d;
    }

    public static long getMinutes(Date ts) {
        long time = ts.getTime();
        long d = time / ONE_MINUTE;
        return d;
    }

    public static long getSeconds(Date ts) {
        long time = ts.getTime();
        long d = time / ONE_SECOND;
        return d;
    }

    /**
     * Creation date: (9/23/2001 5:18:53 PM)
     * @return java.lang.String
     * @param dt java.util.Date
     */
    public static String getSimpleDateStr(java.util.Date dt) {
        try {
            if (dt == null) return "";
            return DateHelper.DATE_FORMATTER.format(dt);
        } catch (Exception e) {
            log.error(e);
            return "";
        }
    }

    public static Date getDate(String dateStr) throws Exception {
        if (StringUtils.isBlank(dateStr))
            return null;

        //try static mm/DD/yy format, which is also used
        try {
            return DATE_FORMATTER3.parse(dateStr);
        } catch (ParseException ex) {
            // go for other formats
        }

        // try static mm/DD/yyyy format, which is used in most places
        try {
            return DATE_FORMATTER.parse(dateStr);
        } catch (ParseException ex) {
            // go for other formats
        }

        String[] allowedFormats = { "MM/dd/yy HH:mm", "MM/dd/yyyy HH:mm",  "MM/dd/yyyy:HH:mm",
                "MM/dd/yy HH:mm:ss", "MM-dd-yy HH:mm", "MM-dd-yy HH:mm:ss", "MM-dd-yy", "yyyy-MM-dd", "MMddyyyyHHmm",
                "MMddyyyyHHmmss", "MMddyyHHmm", "MMddyyHHmmss", "yyyyMMddHHmmssSSSSZ", // HL7 Timestamp
                "yyyyMMddHHmmssSSSS", // HL7 Timestamp
                "yyyyMMddHHmmssZ", // HL7 Timestamp
                "yyyyMMddHHmmss", "yyyyMMddHHmm", "yyyyMMdd", "MMddyy", "MMM dd, yy", "MM/dd/yy", "MM/dd/yyyy" };

        for (int n = 0; n < allowedFormats.length; ++n) {
            FastDateFormat sdf = FastDateFormat.getInstance(allowedFormats[n]);
            //df.setLenient(false);

            try {
                // dozen: no need to adjust year in case of yy; see -80/+20 year rule in SimpleDateFormat
                return sdf.parse(dateStr.trim());
            } catch (ParseException ex) {
                // next format, pls?
            }
        }

        throw new ParseException("Unsupported date format:" + dateStr, 0);
    }

    public static Date getDateFromExcelSerialNumber(String str) throws Exception {
        String s = NumberHelper.clean(str, null);
        return getDateFromExcelSerialNumber(Integer.parseInt(s));
    }

    public static Date getDateFromExcelSerialNumber(Integer number) throws Exception {
        Date base = getDate("01/01/1900");
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DATE, number);
        return c.getTime();
    }

}
