package jp.co.jun.edi.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Objects;

import jp.co.jun.edi.entity.key.VDelischeCsvKey;
import jp.co.jun.edi.type.DelischeProductionStatusType;

/**
 * 業務ロジックユーティリティ.
 */
public final class BusinessUtils {
    private static final int ADD_CNT_AT_SATUADAY = 1;
    private static final int ADD_CNT_AT_FRIDAY = 2;
    private static final int ADD_CNT_AT_THURSDAY = 3;
    private static final int ADD_CNT_AT_WEDNESDAY = 4;
    private static final int ADD_CNT_AT_TUESDAY = 5;
    private static final int ADD_CNT_AT_MONDAY = 6;

    private static final int ONLY_SERIAL_NO = 5; // 品番が5桁の場合は品種なし、通番のみ
    private static final int ONLY_PART_NO_KIND_2 = 2; // 品番が2桁または3桁の場合は通番なし、品種のみ
    private static final int ONLY_PART_NO_KIND_3 = 3; // 品番が2桁または3桁の場合は通番なし、品種のみ
    private static final int PART_NO_KIND_2 = 7; // 品番が7桁の場合は品種2桁
    private static final int FULL = 8; // 品番が8桁の場合は品種3桁

    /**
     */
    private BusinessUtils() {
    }

    /**
     * 以下の条件に従い、MD週変換する.
     * ・週の初めは、月曜日.
     * ・1/1が日曜日の場合は、その週は第1週とする.
     * ・翌年の1/1が含まれる週は、翌年の第1週とする.
     * @param deliveryAt 納期
     * @return MD週
     */
    public static Integer formatMdWeek(final Date deliveryAt) {
        if (Objects.isNull(deliveryAt)) {
            return null;
        }
        // 日付から週を取得
        LocalDateTime ldt = LocalDateTime.ofInstant(deliveryAt.toInstant(), ZoneId.systemDefault());
        DayOfWeek dayOfWeek = ldt.getDayOfWeek();
        int addCnt = 0;
        switch (dayOfWeek) {
        case MONDAY: // 月曜日の場合は6日追加
            addCnt = ADD_CNT_AT_MONDAY;
            break;
        case TUESDAY: // 火曜日の場合は5日追加
            addCnt = ADD_CNT_AT_TUESDAY;
            break;
        case WEDNESDAY: // 木曜日の場合は4日追加
            addCnt = ADD_CNT_AT_WEDNESDAY;
            break;
        case THURSDAY: // 水曜日の場合は3日追加
            addCnt = ADD_CNT_AT_THURSDAY;
            break;
        case FRIDAY: // 金曜日の場合は2日追加
            addCnt = ADD_CNT_AT_FRIDAY;
            break;
        case SATURDAY: // 土曜日の場合は1日追加
            addCnt = ADD_CNT_AT_SATUADAY;
            break;
        default: // 日曜日の場合は追加なし
            break;
        }
        // 日付加算
        ldt.plusDays(addCnt);

        // 加算後、週番号取得
        WeekFields wf = WeekFields.of(DayOfWeek.MONDAY, 1);
        return ldt.get(wf.weekOfYear());
    }

    /**
     * 生産ステータスフラグをデリスケCSVファイル出力形式に整形する.
     * @param productionStatus 生産工程区分
     * @return 整形後生産工程区分
     */
    public static String formatDelischeProductionStatus(final DelischeProductionStatusType productionStatus) {
        switch (productionStatus) {
        case NO_LATE:
            return "○";
        case LATE:
            return "！";
        case NO_DATA:
            return "×";
        default:
            return "";
        }
    }

    /**
     * 納期遅延件数を整形する.
     * @param lateDeliveryAtCnt 納期遅延件数
     * @return 整形後納期遅延件数
     */
    public static String formatLateDeliveryAtCnt(final Integer lateDeliveryAtCnt) {
        if (lateDeliveryAtCnt == null || lateDeliveryAtCnt == 0) {
            return "";
        }
        return "！";
    }

    /**
     * 納品依頼回数を整形する.
     * @param vDelischeCsvKey デリスケCSVキー
     * @return 整形後納品依頼回数
     */
    public static String formatDeliveryCount(final VDelischeCsvKey vDelischeCsvKey) {
        if (vDelischeCsvKey == null) {
            return "未承認";
        }

        final Integer deliveryCount = vDelischeCsvKey.getDeliveryCount();
        if (deliveryCount == null || deliveryCount == 0) {
            return "未承認";
        }
        return String.format("%02d", deliveryCount);
    }

    /**
     * 品番にハイフンを付けて返す.
     *
     * @param partNo 品番データ
     * @return 整形した品番
     */
    public static String formatPartNo(final String partNo) {
        String fomratPartNo = "";
        StringBuilder sb = new StringBuilder(partNo);
        switch (partNo.length()) {
        // 品種なし
        case ONLY_SERIAL_NO:
            fomratPartNo = "-" + partNo;
            break;
        // 品種2桁
        case ONLY_PART_NO_KIND_2:
        case PART_NO_KIND_2:
            fomratPartNo = sb.insert(ONLY_PART_NO_KIND_2, "-").toString();
            break;
        // 品種3桁
        case ONLY_PART_NO_KIND_3:
        case FULL:
            fomratPartNo = sb.insert(ONLY_PART_NO_KIND_3, "-").toString();
            break;
        default:
            fomratPartNo = partNo;
            break;
        }
        return fomratPartNo;
    }
}
