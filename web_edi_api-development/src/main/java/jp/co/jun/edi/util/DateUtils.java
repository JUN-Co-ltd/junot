package jp.co.jun.edi.util;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * 日付フォーマットユーティリティ.
 */
public final class DateUtils {
    private static final long MILLISECOND = 1000L;

    /** 「yyyy/m/d」形式のフォーマッター. */
    private static final DateTimeFormatter YMD_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu/M/d")
            // 日付を厳密にチェック
            .withResolverStyle(ResolverStyle.STRICT);

    /** 週の開始を月曜日で計算する週計算用のフィールド. */
    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;

    /** 年の開始位置. */
    private static final int YYYY_START_INDEX = 0;

    /** 年の終了位置. */
    private static final int YYYY_END_INDEX = 4;

    /** 週番号の開始位置. */
    private static final int WW_START_INDEX = 4;

    /** 週番号の終了位置. */
    private static final int WW_END_INDEX = 6;

    private static final int NON_FORMAT_DATE_LENGTH = 8;

    private static final int NON_FORMAT_TIME_LENGTH = 6;

    /** 月度開始日. */
    private static final int MONTHLY_FROM_DATE = 21;

    /** 月度終了日. */
    private static final int MONTHLY_TO_DATE = 20;

    /**
     */
    private DateUtils() {
    }

    /**
     * 日付の時分秒を切り捨てる.
     * @param date 日付
     * @return 時分秒を切り捨てた日付
     */
    public static Date truncateDate(final Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        // 時分秒を切り捨て
        return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
    }

    /**
     * yyyy/MM/ddに整形する.
     * @param date 日付
     * @return 整形後日付
     */
    public static String formatYMD(final Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }

    /**
     * 指定フォーマットに日付型を整形する.
     * @param date 日付
     * @param format フォーマット
     * @return 整形後日付
     */
    public static String formatFromDate(final Date date, final String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 現在日時（システムデフォルトタイムゾーン）を取得する.
     * @return 現在日時（システムデフォルトタイムゾーン）
     */
    public static Date createNow() {
        return Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
    }

    /**
     * String型の日付をDateに変換.
     *
     * @param strDate 日付 (yyyyMMdd or yyyy/MM/dd)
     * @return Date型に変換した日付
     */
    public static Date stringToDate(final String strDate) {

        LocalDate ld;

        if (strDate.length() == NON_FORMAT_DATE_LENGTH) {
            ld = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            ld = LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * String型の時間をDateに変換.
     *
     * @param  strTime 時間 (HHmmss or HH:mm:ss)
     * @return Date型に変換した時間(24時間表記。年月日はnull)
     */
    public static Date stringToTime(final String strTime) {
        LocalTime lt;
        if (strTime.length() == NON_FORMAT_TIME_LENGTH) {
            lt = LocalTime.parse(strTime, DateTimeFormatter.ofPattern("HHmmss"));
        } else {
            lt = LocalTime.parse(strTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        }

        // java.sqlはDateを継承しているため、継承元クラスのDateで返却も可能
        // (この方法に限り日付を設定せずに利用することが可能)
        return java.sql.Time.valueOf(lt);
    }

    /**
     * 「yyyy/M/d」形式の文字列をDateに変換.
     * 日付が空の場合、nullに変換.
     *
     * @param strDate 日付
     * @return Date型に変換した日付
     */
    public static Date stringYMDToDateOrNull(final String strDate) {
        if (StringUtils.isEmpty(strDate)) {
            return null;
        }

        return Date.from(stringYMDToLocalDate(strDate).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 日付が一致するか判断する.
     * 日付が両方ともnullの場合は一致として、trueを返却。
     * 日付が片方あnullの場合は不一致として、falseを返却。
     *
     * @param date1 日付1
     * @param date2 日付2
     * @return 時分秒を切り捨てた日付
     */
    public static boolean isSameDay(final Date date1, final Date date2) {
        if (Objects.isNull(date1) && Objects.isNull(date2)) {
            return true;
        }
        if (Objects.isNull(date1) || Objects.isNull(date2)) {
            return false;
        }
        // 日付が両方ともnullでない場合：
        return org.apache.commons.lang3.time.DateUtils.isSameDay(date1, date2);
    }

    /**
     * 日付(ミリ秒まで)が一致するか判断する.
     * 日付が両方ともnullの場合は一致として、trueを返却。
     * 日付が片方あnullの場合は不一致として、falseを返却。
     *
     * @param date1 日付1
     * @param date2 日付2
     * @return true:一致　false:不一致
     */
    public static boolean isSameTimestanp(final Date date1, final Date date2) {
        if (Objects.isNull(date1) && Objects.isNull(date2)) {
            return true;
        }
        if (Objects.isNull(date1) || Objects.isNull(date2)) {
            return false;
        }

        // 日付が両方ともnullでない場合：
        return date1.equals(date2);
    }

    /**
     * UNIX時間に変換する.ミリ秒は除く.
     *
     * @param date 日時
     * @return UNIX時間
     */
    public static long toUnixTimestamp(final Date date) {
        return date.getTime() / MILLISECOND;
    }

    /**
     * 「yyyyww」（年＋週番号）の文字列から日付に変換.
     *
     * <pre>
     * 日付は該当週の(週末)日曜日とする。
     * 月曜日基準で1/1が含まれる週を第1週とする。
     * 12/31が日曜日の場合は、次の週が第1週となり、それ以外はその週が第1週とする。
     * </pre>
     *
     * @param strDate 「yyyyww」（年＋週番号）の文字列
     * @return 日付
     */
    public static Date stringYYYYWWToDate(final String strDate) {
        final LocalDate ld = LocalDate.now()
                .with(WEEK_FIELDS.weekBasedYear(), Integer.parseInt(strDate.substring(YYYY_START_INDEX, YYYY_END_INDEX)))
                .with(WEEK_FIELDS.weekOfWeekBasedYear(), Integer.parseInt(strDate.substring(WW_START_INDEX, WW_END_INDEX)))
                .with(WEEK_FIELDS.dayOfWeek(), DayOfWeek.SUNDAY.getValue());

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 「yyyy/M/d」形式の文字列から、「yyyyww」（年＋週番号）に変換.
     *
     * <pre>
     * 月曜日基準で1/1が日曜日の場合は、その週は第1週とする。
     * その他の場合は、12/31が含まれる週は翌年の第1週とする。
     * </pre>
     *
     * @param strDate 「yyyy/M/d」形式の文字列
     * @return 「yyyyww」（年＋週番号）の文字列
     */
    public static String stringYMDToYYYYWW(final String strDate) {
        final LocalDate ld = stringYMDToLocalDate(strDate);

        return String.format("%04d%02d", ld.get(WEEK_FIELDS.weekBasedYear()), ld.get(WEEK_FIELDS.weekOfWeekBasedYear()));
    }

    /**
     * 日付形式の文字列から、週番号を計算する.
     *
     * <pre>
     * 月曜日基準で1/1が日曜日の場合は、その週は第1週とする。
     * その他の場合は、12/31が含まれる週は翌年の第1週とする。
     * </pre>
     *
     * @param date 日付
     * @return 週番号
     */
    public static int calcWeek(final Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(WEEK_FIELDS.weekOfWeekBasedYear());
    }

    /**
     * 「yyyy/M/d」形式の文字列をLocalDateに変換.
     *
     * @param strDate 日付
     * @return LocalDate型に変換した日付
     */
    public static LocalDate stringYMDToLocalDate(final String strDate) {
        return LocalDate.parse(strDate, YMD_FORMATTER);
    }

    /**
     * @param date 基準日
     * @param days 加算日数
     * @return 加算した日付
     */
    public static Date plusDays(final Date date, final int days) {
        final ZoneId zone = ZoneId.systemDefault();

        final LocalDateTime resultLdt = LocalDateTime.ofInstant(date.toInstant(), zone).plusDays(days);
        final Instant resultInstant = ZonedDateTime.of(resultLdt, zone).toInstant();
        return Date.from(resultInstant);
    }

    /**
     * @param date 基準日
     * @param days 減算日数
     * @return 減算した日付
     */
    public static Date minusDays(final Date date, final int days) {
        final ZoneId zone = ZoneId.systemDefault();

        final LocalDateTime resultLdt = LocalDateTime.ofInstant(date.toInstant(), zone).minusDays(days);
        final Instant resultInstant = ZonedDateTime.of(resultLdt, zone).toInstant();
        return Date.from(resultInstant);
    }

    /**
     * 年度と月度をDate型の開始年月日に変換する.
     *
     * <pre>
     * 年度がない場合、NULLを返却する。
     * 月度がない場合、1月を設定する。
     * 指定された年月の前月の21日を返却する。
     * </pre>
     *
     * @param year 年度
     * @param monthly 月度
     * @return 開始年月日
     */
    public static Date toFromDate(final Integer year, final Integer monthly) {
        if (Objects.isNull(year)) {
            // 年度がない場合、NULLを返却する
            return null;
        }

        // 指定月の前月の21日から
        return toMonthlyToDate(year.intValue(),
                // 月度がない場合、1月を設定する
                defaultMonth(monthly, Calendar.JANUARY));
    }

    /**
     * 年度と月度をDate型の終了年月日に変換する.
     *
     * <pre>
     * 年度がない場合、NULLを返却する。
     * 月度がない場合、12月を設定する。
     * 指定された年月の20日を返却する。
     * </pre>
     *
     * @param year 年度
     * @param monthly 月度
     * @return 終了年月日
     */
    public static Date toToDate(final Integer year, final Integer monthly) {
        if (Objects.isNull(year)) {
            // 年度がない場合、NULLを返却する
            return null;
        }

        // 指定月の20日まで
        return toMonthlyToDate(year.intValue(),
                // 月度がない場合、12月を設定する
                defaultMonth(monthly, Calendar.DECEMBER));
    }

    /**
     * 月度から、MONTHカレンダ・フィールドの設定に使用する値を返却する.
     * Month値は0から始まる(1月は0になる).
     *
     * @param monthly 月度
     * @param defaultMonth 月度がNULLの場合の初期値
     * @return 終了年月日
     */
    public static int defaultMonth(final Integer monthly, final int defaultMonth) {
        if (Objects.isNull(monthly)) {
            // 月度がない場合、defaultMonthを返却する
            return defaultMonth;
        }

        return monthly.intValue() - 1;
    }

    /**
     * 月度の開始年月日を返す.
     * 月日は指定月度の前月の21日を返す.
     * 指定月度が1月であれば指定年の前年の12月21日を返す.
     *
     * @param year 年
     * @param month MONTHカレンダ・フィールドの設定に使用する値。Month値は0から始まる(1月は0になる)。
     * @return 開始年月日
     */
    public static Date toMonthlyFromDate(final int year, final int month) {
        final Calendar calendar = Calendar.getInstance();
        final int fromYear;
        final int fromMonth;

        if (Calendar.JANUARY == month) {
            fromYear = year - 1;
            fromMonth = Calendar.DECEMBER;
        } else {
            fromYear = year;
            fromMonth = month - 1;
        }

        calendar.set(fromYear, fromMonth, MONTHLY_FROM_DATE);

        return new Date(calendar.getTime().getTime());
    }

    /**
     * 月度の終了年月日を返す.
     * 月日は指定月度の前月の20日を返す.
     *
     * @param year 年
     * @param month MONTHカレンダ・フィールドの設定に使用する値。Month値は0から始まる(1月は0になる)。
     * @return 開始年月日
     */
    public static Date toMonthlyToDate(final int year, final int month) {
        final Calendar calendar = Calendar.getInstance();

        // 指定月の20日まで
        calendar.set(year, month, MONTHLY_TO_DATE);

        return new Date(calendar.getTime().getTime());
    }

    /**
     * 日付の加算・減算する.
     *
     * <pre>
     * カレンダのルールに基づいて、指定された時間量を指定されたカレンダ・フィールドに加算または減算します。
     * たとえば、カレンダの現在の時間から5日を引く場合は、次の呼出しを実行します。
     * add(new Date("2020/01/01"), Calendar.DAY_OF_MONTH, -5).
     * </pre>
     *
     * @param date
     *            日付
     * @param field
     *            カレンダ・フィールド
     * @param amount
     *            フィールドに追加される日付または時間の量
     * @return 日付
     */
    public static Date add(final Date date, final int field, final int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * date1 より date2 の日付が遅い場合、date2を返す.
     *
     * @param date1
     *            日付
     * @param date2
     *            比べる日付
     * @return date1 と date2 どちらか遅い日付
     */
    public static Date whicheverComesLater(final Date date1, final Date date2) {
        if (date1.after(date2)) {
            return date2;
        }
        return date1;
    }
}
