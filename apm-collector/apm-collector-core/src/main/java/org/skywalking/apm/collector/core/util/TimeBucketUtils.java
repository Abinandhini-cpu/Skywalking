package org.skywalking.apm.collector.core.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import org.skywalking.apm.collector.core.framework.UnexpectedException;

/**
 * @author pengys5
 */
public enum TimeBucketUtils {
    INSTANCE;

    private final SimpleDateFormat dayDateFormat = new SimpleDateFormat("yyyyMMdd");
    private final SimpleDateFormat hourDateFormat = new SimpleDateFormat("yyyyMMddHH");
    private final SimpleDateFormat minuteDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    private final SimpleDateFormat secondDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public long getMinuteTimeBucket(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String timeStr = minuteDateFormat.format(calendar.getTime());
        return Long.valueOf(timeStr);
    }

    public long getSecondTimeBucket(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String timeStr = secondDateFormat.format(calendar.getTime());
        return Long.valueOf(timeStr);
    }

    public long getHourTimeBucket(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String timeStr = hourDateFormat.format(calendar.getTime()) + "00";
        return Long.valueOf(timeStr);
    }

    public long getDayTimeBucket(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String timeStr = dayDateFormat.format(calendar.getTime()) + "0000";
        return Long.valueOf(timeStr);
    }

    public long changeTimeBucket2TimeStamp(String timeBucketType, long timeBucket) {
        if (TimeBucketType.SECOND.name().toLowerCase().equals(timeBucketType.toLowerCase())) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.valueOf(String.valueOf(timeBucket).substring(0, 4)));
            calendar.set(Calendar.MONTH, Integer.valueOf(String.valueOf(timeBucket).substring(4, 6)) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(String.valueOf(timeBucket).substring(6, 8)));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(String.valueOf(timeBucket).substring(8, 10)));
            calendar.set(Calendar.MINUTE, Integer.valueOf(String.valueOf(timeBucket).substring(10, 12)));
            calendar.set(Calendar.SECOND, Integer.valueOf(String.valueOf(timeBucket).substring(12, 14)));
            return calendar.getTimeInMillis();
        } else if (TimeBucketType.MINUTE.name().toLowerCase().equals(timeBucketType.toLowerCase())) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, Integer.valueOf(String.valueOf(timeBucket).substring(0, 4)));
            calendar.set(Calendar.MONTH, Integer.valueOf(String.valueOf(timeBucket).substring(4, 6)) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(String.valueOf(timeBucket).substring(6, 8)));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(String.valueOf(timeBucket).substring(8, 10)));
            calendar.set(Calendar.MINUTE, Integer.valueOf(String.valueOf(timeBucket).substring(10, 12)));
            return calendar.getTimeInMillis();
        } else {
            throw new UnexpectedException("time bucket type must be second or minute");
        }
    }

    public long getFiveSecondTimeBucket(long secondTimeBucket) {
        long mantissa = secondTimeBucket % 10;
        if (mantissa < 5) {
            return (secondTimeBucket / 10) * 10;
        } else if (mantissa == 5) {
            return secondTimeBucket;
        } else {
            return ((secondTimeBucket / 10) + 1) * 10;
        }
    }

    public long changeToUTCTimeBucket(long timeBucket) {
        String timeBucketStr = String.valueOf(timeBucket);

        if (TimeZone.getDefault().getID().equals("GMT+08:00") || timeBucketStr.endsWith("0000")) {
            return timeBucket;
        } else {
            return timeBucket - 800;
        }
    }

    public enum TimeBucketType {
        SECOND, MINUTE, HOUR, DAY
    }
}
