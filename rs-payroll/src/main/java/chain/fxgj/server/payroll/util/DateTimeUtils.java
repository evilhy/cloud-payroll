package chain.fxgj.server.payroll.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 日期工具类
 *
 * @param
 * @author
 */
public class DateTimeUtils {

    public static final DateTimeFormatter DATETIME_FORMATTER_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATE_FORMATTER_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static final DateTimeFormatter DATE_FORMATTER_YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final DateTimeFormatter TIME_FORMATTER_HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    public static final SimpleDateFormat SDFMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // public static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final int DATASTR_LEN = 6;


    /**
     * 字符串  yyyy-MM-dd  转  LocalDate 对象
     *
     * @param dateStr
     * @return LocalDate
     */
    public static LocalDate parseLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }


    /**
     * 字符串  yyyyMMdd  转  LocalDate 对象
     *
     * @param dateStr
     * @return LocalDate
     */
    public static LocalDate parseLocalDateYyyymmdd(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER_YYYYMMDD);
    }


    /**
     * 字符串  yyyy-MM-dd HH:mm:ss  转  LocalDateTime 对象
     *
     * @param dateTimeStr
     * @return LocalDateTime
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 字符串  yyyy-MM-dd HH:mm:ss..SSS  转  LocalDateTime 对象
     *
     * @param dateTimeSsstr
     * @return LocalDateTime
     */
    public static LocalDateTime parseLocalDateTimeSss(String dateTimeSsstr) {
        return LocalDateTime.parse(dateTimeSsstr, DATETIME_FORMATTER_SSS);
    }


    /**
     * 字符串  HH:mm:ss 转  LocalTime 对象
     *
     * @param timeStr
     * @return LocalDate
     */
    public static LocalTime parseLocalTime(String timeStr) {
        return LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * LocalDate 对象  转  字符串  yyyy-MM-dd
     *
     * @param date
     * @return String
     */
    public static String formatLocalDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    /**
     * LocalDate 对象  转  字符串  yyyyMMdd
     *
     * @param date
     * @return String
     */
    public static String formatLocalDateYyyymmdd(LocalDate date) {
        return date.format(DATE_FORMATTER_YYYYMMDD);
    }


    /**
     * LocalDateTime 对象  转  字符串  yyyy-MM-dd HH:mm:ss
     *
     * @param datetime
     * @return String
     */
    public static String formatLocalDateTime(LocalDateTime datetime) {
        return datetime.format(DATETIME_FORMATTER);
    }


    /**
     * LocalDateTime 对象  转  字符串  yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param dateTime
     * @return String
     */
    public static String formatLocalDateTimeSss(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER_SSS);
    }


    /**
     * LocalTime 对象  转  字符串  HH:mm:ss
     *
     * @param time
     * @return String
     */
    public static String formatLocalTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    /**
     * 日期相隔天数
     *
     * @param startDateInclusive
     * @param endDateExclusive
     * @return
     */
    public static int periodDays(LocalDate startDateInclusive, LocalDate endDateExclusive) {
        return Period.between(startDateInclusive, endDateExclusive).getDays();
    }

    /**
     * 日期相隔小时
     *
     * @param startInclusive
     * @param endExclusive
     * @return
     */
    public static long durationHours(Temporal startInclusive, Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive).toHours();
    }

    /**
     * 日期相隔分钟
     *
     * @param startInclusive
     * @param endExclusive
     * @return
     */
    public static long durationMinutes(Temporal startInclusive, Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMinutes();
    }

    /**
     * 日期相隔毫秒数
     *
     * @param startInclusive
     * @param endExclusive
     * @return
     */
    public static long durationMillis(Temporal startInclusive, Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive).toMillis();
    }

    /**
     * 返回当前的日期
     *
     * @return 示例：2019-08-31
     */
    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now();
    }


    /**
     * 返回当前时间
     *
     * @return 示例：14:41:44.556
     */
    public static LocalDate getCurrentLocalDate(String dataStr) throws ParseException {
        if (dataStr.length() == DATASTR_LEN) {
            dataStr = dataStr.concat("01");
        }
        return LocalDate.parse(SDFMAT.format(SDF.parse(strToDateFormat(dataStr))), DATETIME_FORMATTER);
    }

    /**
     * 返回当前时间
     *
     * @return 示例：14:41:44.556
     */
    public static LocalTime getCurrentLocalTime() {
        return LocalTime.now();
    }


    /**
     * 返回当前的日期
     *
     * @return
     * @Description:2019-08-31T14:42:38.866
     */
    public static LocalDateTime get(String dataStr) throws ParseException {


        return LocalDateTime.parse(SDFMAT.format(SDF.parse(strToDateFormat(dataStr))), DATETIME_FORMATTER);
    }

    /**
     * 返回当前的日期
     *
     * @return
     * @Description:2019-08-31T14:42:38.866
     */
    public static LocalDateTime get() {
        return LocalDateTime.now();
    }

    /**
     * 返回当前年份
     *
     * @return
     * @Description: 2019
     */
    public static int getYear() {
        return get().getYear();
    }

    /**
     * int 转  LocalDateTime
     *
     * @return
     */
    public static LocalDateTime withYear(int year) {
        return get().withYear(year);
    }

    /**
     * @return
     */
    public static int getMonth() {
        return get().getMonthValue();
    }

    /**
     * @return
     */
    public static LocalDateTime firstDayOfThisYear(int year) {
        return withYear(year).with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
    }

    /**
     * @param year
     * @return String
     * @Title: getFirstDayOfThisYear
     * @Description: 获取设置所属年最初时间
     */
    public static String getFirstDayOfThisYear(int year) {
        LocalDateTime firstDayOfThisYear = firstDayOfThisYear(year);
        return DATETIME_FORMATTER.format(firstDayOfThisYear);
    }

    /**
     * @return
     */
    public static LocalDateTime lastDayOfThisYear(int year) {
        return withYear(year).with(TemporalAdjusters.lastDayOfYear()).with(LocalTime.MAX);
    }

    /**
     * @param year
     * @return String
     * @Title: getLastDayOfThisYear
     * @Description: 获取设置所属年最后时间
     */
    public static String getLastDayOfThisYear(int year) {
        LocalDateTime lastDayOfThisYear = lastDayOfThisYear(year);
        return DATETIME_FORMATTER.format(lastDayOfThisYear);
    }

    /**
     * @return String
     * @Title: getFirstDayOfThisMonth
     * @Description: 获取本月的第一天
     */
    public static String getFirstDayOfThisMonth() {
        LocalDateTime firstDayOfThisYear = get().with(TemporalAdjusters.firstDayOfMonth());
        return DATETIME_FORMATTER.format(firstDayOfThisYear);
    }

    /**
     * @return String
     * @Title: getFirstDayOfThisMonth
     * @Description: 获取本月的最末天
     */
    public static String getLastDayOfThisMonth() {
        LocalDateTime firstDayOfThisYear = get().with(TemporalAdjusters.lastDayOfMonth());
        return DATETIME_FORMATTER.format(firstDayOfThisYear);
    }

    /**
     * @param days
     * @return LocalDateTime
     * @Title: plusDays
     * @Description: 当前日期向后推多少天
     */
    public static LocalDateTime plusDays(int days) {
        return get().plusDays(days);
    }

    /**
     * @param year
     * @param month
     * @return LocalDateTime
     * @Title: firstDayOfWeekInYearMonth
     * @Description: 获取指定年月的第一个周一
     */
    public static LocalDateTime firstDayOfWeekInYearMonth(int year, int month) {
        return get().withYear(year).withMonth(month).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    }

    /**
     * @return LocalDateTime
     * @Title: todayStart
     * @Description: 当天开始时间
     */
    public static LocalDateTime todayStart() {
        return LocalDateTime.of(getCurrentLocalDate(), LocalTime.MIN);
    }


    /**
     * @return LocalDateTime
     * @Title: todayEnd
     * @Description: 当天结束时间
     */
    public static LocalDateTime todayEnd() {
        return LocalDateTime.of(getCurrentLocalDate(), LocalTime.MAX);
    }

    /**
     * @return LocalDateTime
     * @Title: todayStart
     * @Description: 前一天 开始时间
     */
    public static LocalDateTime beforeTodayStart() {
        return LocalDateTime.of(getCurrentLocalDate().minusDays(1), LocalTime.MIN);
    }

    /**
     * @return LocalDateTime
     * @Title: todayEnd
     * @Description: 前一天 结束时间
     */
    public static LocalDateTime beforeTodayEnd() {
        return LocalDateTime.of(getCurrentLocalDate().minusDays(1), LocalTime.MAX);
    }

    /**
     * 指定日期  开始时间
     *
     * @param localDate
     * @return
     * @Title: dayStart
     * @Description: 指定日期  开始时间
     */
    public static LocalDateTime dayStart(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    /**
     * 指定日期  结束时间
     *
     * @param localDate
     * @return
     * @Title: dayEnd
     * @Description: 指定日期  结束时间
     */
    public static LocalDateTime dayEnd(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MAX);
    }


    /**
     * @return String
     * @Title: getStartDayOfWeekToString
     * @Description: 获取周第一天
     */
    public static String getStartDayOfWeekToString() {
        return formatLocalDate(getStartDayOfWeek());
    }

    /**
     * @return
     */
    public static LocalDate getStartDayOfWeek() {
        TemporalAdjuster firstOfWeek = TemporalAdjusters.ofDateAdjuster(localDate -> localDate.minusDays(localDate
                .getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()));
        return getCurrentLocalDate().with(firstOfWeek);
    }

    /**
     * @return String
     * @Title: getEndDayOfWeekToString
     * @Description: 获取周最后一天
     */
    public static String getEndDayOfWeekToString() {
        return formatLocalDate(getEndDayOfWeek());
    }

    /**
     * @return LocalDateTime
     * @Description: 当前开始时间
     */
    public static LocalDateTime getNow() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
    }

    /**
     * @return LocalDateTime
     * @Description: 指定时间添加一周
     */
    public static LocalDateTime minusOneWeek(LocalDateTime now) {
        return now.minus(1, ChronoUnit.WEEKS);
    }

    public static LocalDate getEndDayOfWeek() {
        TemporalAdjuster lastOfWeek = TemporalAdjusters.ofDateAdjuster(localDate -> localDate.plusDays(
                DayOfWeek.SUNDAY.getValue() - localDate.getDayOfWeek().getValue()));
        return getCurrentLocalDate().with(lastOfWeek);
    }

    //public static String formatLocalDateTime(LocalDateTime datetime) {
    //    return datetime.format(DATETIME_FORMATTER);
    //}

    public static String formatLocalDateTime() {
        return formatLocalDateTime(LocalDateTime.now());
    }

    public static LocalDateTime date2LocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * 将字符串格式yyyyMMdd的字符串转为日期，格式"yyyy-MM-dd"
     *
     * @param date 日期字符串
     * @return 返回格式化的日期
     * @throws ParseException 分析时意外地出现了错误异常
     */
    public static String strToDateFormat(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setLenient(false);
        Date newDate = formatter.parse(date);
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(newDate);
    }


    /**
     * 当月的开始日期
     */
    public static LocalDateTime firstDayOfThisMonth() throws ParseException {
        LocalDate today = getCurrentLocalDate();
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        String startData = formatLocalDate(firstDayOfThisMonth);
        return LocalDateTime.parse(SDFMAT.format(SDF.parse(startData)), DATETIME_FORMATTER);
    }

    /**
     * 当月的结束日期
     */
    public static LocalDateTime lastDayOfThisMonth() throws ParseException {
        LocalDate today = getCurrentLocalDate();
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        String endData = formatLocalDate(lastDayOfThisMonth);
        LocalDateTime.parse(SDFMAT.format(SDF.parse(endData)), DATETIME_FORMATTER);
        return LocalDateTime.of(lastDayOfThisMonth, LocalTime.MAX);
    }

    /**
     * 指定日期  开始时间
     *
     * @param localDate
     * @return
     * @Title: dayStart
     * @Description: 指定日期  开始时间
     */
    public static LocalDateTime firstDayOfThisMonth(LocalDate localDate) throws ParseException {

        LocalDate today = localDate;
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        String startData = formatLocalDate(firstDayOfThisMonth);
        return LocalDateTime.parse(SDFMAT.format(SDF.parse(startData)), DATETIME_FORMATTER);
    }


    /**
     * 指定日期  结束时间
     *
     * @param localDate
     * @return
     * @Title: dayEnd
     * @Description: 指定日期  结束时间
     */
    public static LocalDateTime lastDayOfThisMonth(LocalDate localDate) throws ParseException {
        LocalDate today = localDate;
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        String endData = formatLocalDate(lastDayOfThisMonth);
        LocalDateTime.parse(SDFMAT.format(SDF.parse(endData)), DATETIME_FORMATTER);
        return LocalDateTime.of(lastDayOfThisMonth, LocalTime.MAX);
    }


    /**
     * 获取当前日期
     *
     * @return yyyyMMdd
     * @Description: 获取当前日期
     */
    public static String getDate() {
        return DateTimeUtils.DATE_FORMATTER_YYYYMMDD.format(DateTimeUtils.get());
    }


    /**
     * 获取当前日期时间
     *
     * @return yyyyMMddHHmmss
     * @Description: 获取当前日期
     */
    public static String getDateTime() {
        return DateTimeUtils.DATE_FORMATTER_YYYYMMDDHHMMSS.format(DateTimeUtils.get());
    }


    /**
     * 按指定时间转换
     *
     * @param localDateTime
     * @return yyyyMMddHHmmss
     * @Description: 获取当前日期
     */
    public static String getDateTime(LocalDateTime localDateTime) {
        return DateTimeUtils.DATE_FORMATTER_YYYYMMDDHHMMSS.format(localDateTime);
    }

    public static LocalDateTime convert(Long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public static Long convert(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取当前时间
     *
     * @return HHmmss
     * @Description: 获取当前日期
     */
    public static String getTime() {
        return DateTimeUtils.TIME_FORMATTER_HHMMSS.format(DateTimeUtils.get());
    }


    public static void main(String[] args) throws ParseException {


        System.out.println(getDate());
        System.out.println(getDateTime());
        System.out.println(getTime());


        System.out.println("======yyyy-MM-dd HH:mm:ss.SSS  与 LocalDateTime 互转 ====");

        System.out.println(formatLocalDateTimeSss(get()));
        System.out.println(parseLocalDateTimeSss(formatLocalDateTimeSss(get())));


        System.out.println("===============");


        System.out.println(getCurrentLocalDate());
        System.out.println(getCurrentLocalTime());
        System.out.println(get());
        System.out.println(getYear());
        System.out.println(withYear(2019));
        System.out.println(withYear(2018));
        System.out.println(getMonth());
        System.out.println(firstDayOfThisYear(2018));
        System.out.println(getFirstDayOfThisYear(2018));
        System.out.println(lastDayOfThisYear(2018));
        System.out.println(getLastDayOfThisYear(2018));
        System.out.println(getFirstDayOfThisMonth());
        System.out.println(getLastDayOfThisMonth());
        System.out.println(plusDays(1));

        System.out.println(firstDayOfWeekInYearMonth(2019, 8));
        System.out.println(todayStart());
        System.out.println(todayEnd());

        System.out.println(beforeTodayStart());
        System.out.println(beforeTodayEnd());

        System.out.println("当前时间 " + get());
        System.out.println("指定日期 " + getCurrentLocalDate("20190808"));

        System.out.println("指定日期【开始时间】 " + dayStart(getCurrentLocalDate("20190808")));
        System.out.println("指定日期【结束时间】 " + dayEnd(getCurrentLocalDate("20190808")));
        System.out.println("当前日期【开始时间】 " + todayStart());
        System.out.println("当前日期【结束时间】 " + todayEnd());

        System.out.println("当前日期 的 前一日 【开始时间】" + beforeTodayStart());
        System.out.println("当前日期 的 前一日 【结束时间】" + beforeTodayEnd());


        System.out.println("当前日期  所在月份 【开始时间】" + firstDayOfThisMonth());
        System.out.println("当前日期  所在月份 【结束时间】" + lastDayOfThisMonth());

        System.out.println("指定日期  所在月份 【开始时间】" + firstDayOfThisMonth(getCurrentLocalDate("20190601")));
        System.out.println("指定日期  所在月份 【结束时间】" + lastDayOfThisMonth(getCurrentLocalDate("20190601")));
        System.out.println("指定月份  所在月份 【开始时间】" + firstDayOfThisMonth(getCurrentLocalDate("201907")));
        System.out.println("指定月份  所在月份 【结束时间】" + lastDayOfThisMonth(getCurrentLocalDate("201907")));


        System.out.println("==== 当月的开始日期     当月的结束时间 ====");


        LocalDate today = LocalDate.now();
        LocalDate firstDayOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfThisMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        System.out.println("===>LocalDate");
        System.out.println(firstDayOfThisMonth);
        System.out.println(lastDayOfThisMonth);


        System.out.println("===>LocalDateTime");
        String startData = formatLocalDate(firstDayOfThisMonth);
        String endData = formatLocalDate(lastDayOfThisMonth);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfmat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println(LocalDateTime.parse(SDFMAT.format(SDF.parse(startData)), df));
        System.out.println(LocalDateTime.parse(SDFMAT.format(SDF.parse(endData).getTime() + 86400000), df));
        System.out.println(LocalDateTime.parse(SDFMAT.format(SDF.parse(endData).getTime()), df));


        System.out.println(LocalDateTime.of(lastDayOfThisMonth, LocalTime.MAX));


        System.out.println("==== =");

        System.out.println("==== 当前月的，前一个月 ====");
        LocalDate date = LocalDate.now();
        date = date.minusMonths(1);
        System.out.println(date);
        LocalDate beforefirstDayOfThisMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate beforelastDayOfThisMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        System.out.println(beforefirstDayOfThisMonth);
        System.out.println(beforelastDayOfThisMonth);


        System.out.println(parseLocalDate("2019-08-31"));

        System.out.println(LocalDate.parse("20190901", DateTimeFormatter.ofPattern("yyyyMMdd")));


    }

}