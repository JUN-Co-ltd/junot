package jp.co.jun.edi.component.materialorder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.entity.MCalendarEntity;
import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.repository.MCalendarRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessDayFlgType;
import jp.co.jun.edi.type.CalendarType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.Data;

/**
 * 資材発注情報登録用のコンポーネント.
 */
@Component
public class MaterialOrderValidateComponent extends GenericComponent {
    /** 印字物有り資材のリソース名. */
    private static final String RESOURCE_MATERIAL_ORDER_MATERIAL_WITH_PRINTED = "materialOrderMaterialWithPrinted";
    /** 発注日のリソース名. */
    private static final String RESOURCE_MATERIAL_ORDER_ORDER_AT = "materialOrderOrderAt";
    /** 希望出荷日のリソース名. */
    private static final String RESOURCE_MATERIAL_ORDER_PREFERRED_SHIPPING_AT = "materialOrderPreferredShippingAt";
    /** アテンション下札のリソース名. */
    private static final String RESOURCE_MATERIAL_ORDER_BOTTOM_BILLATENTION = "materialOrderBottomBillAtention";

    /** 営業日指定. */
    private enum SpcifyBusinessDay {
        /** 直近営業日. */
        LATEST_BUSINESS_DAY,
        /** 翌営業日. */
        NEXT_BUSINESS_DAY,
        /** 翌々営業日. */
        TWO_BUSINESS_DAYS_AFTER;
    };

    // 共通のカラーコード：00
    private static final String COMMON_COLOR_CODE = "00";

    @Autowired
    private MCalendarRepository mCalendarRepository;

    /**
     * 検証用ビルダーを生成する.
     *
     * @return {@link ValidateBuilder}
     */
    public Validator getValidator() {
        return new Validator();
    }

    /**
     * バリデーター.
     */
    @Data
    public class Validator {
        /** 言語. */
        private Locale locale;

        /** バリデート対象データ. */
        private FukukitaruOrderModel materialOrder;

        /** バリデーションの結果. */
        private List<ResultMessage> resultMessages;

        /**
         * @param locale
         *            言語.
         * @return {@link Validator}
         */
        public Validator locale(final Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * @param materialOrder
         *            資材発注情報.
         * @return {@link Validator}
         */
        public Validator materialOrder(final FukukitaruOrderModel materialOrder) {
            this.materialOrder = materialOrder;
            return this;
        }

        /**
         * @return {@link FukukitaruOrderModel}
         */
        public List<ResultMessage> validate() {
            this.resultMessages = new ArrayList<>();

            // 登録/更新/確定時に資材が入力されているか検証
            validateMaterial();

            // 登録/更新/確定時、確定済以外の資材発注の場合のみチェックする
            if (this.materialOrder.getConfirmStatus() == null
                    || this.materialOrder.getConfirmStatus() != FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED) {
                // 発注日の検証
                validateOrderAt();

                // 希望出荷日の検証
                validatePreferredShippingAt();
            }

            // 資材発注下札画面の場合のみチェックする
            if (this.materialOrder.getOrderType() == FukukitaruMasterOrderType.HANG_TAG
                    || this.materialOrder.getOrderType() == FukukitaruMasterOrderType.HANG_TAG_KOMONO) {
                // アテンション下札の検証
                validateBottomBillAtention();
            }

            return this.resultMessages;
        }

        /**
         * 資材が入力されているか検証.
         */
        private void validateMaterial() {
            if (this.materialOrder.getOrderType() == FukukitaruMasterOrderType.WASH_NAME
                    || this.materialOrder.getOrderType() == FukukitaruMasterOrderType.WASH_NAME_KOMONO) {
                // 資材発注洗濯ネーム画面の場合のチェック
                if (CollectionUtils.isEmpty(this.materialOrder.getOrderSkuWashName()) && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuAttentionName())
                        && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuWashAuxiliary())) {
                    // 資材がいずれも入力されていないためエラー
                    this.resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_007).resource(RESOURCE_MATERIAL_ORDER_MATERIAL_WITH_PRINTED));
                }
            } else if (this.materialOrder.getOrderType() == FukukitaruMasterOrderType.HANG_TAG
                    || this.materialOrder.getOrderType() == FukukitaruMasterOrderType.HANG_TAG_KOMONO) {
                // 資材発注下札画面の場合のチェック
                if (CollectionUtils.isEmpty(this.materialOrder.getOrderSkuBottomBill()) && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuAttentionTag())
                        && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuBottomBillAttention())
                        && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuBottomBillNergyMerit())
                        && CollectionUtils.isEmpty(this.materialOrder.getOrderSkuBottomBillAuxiliaryMaterial())) {
                    // 資材がいずれも入力されていないためエラー
                    this.resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_007).resource(RESOURCE_MATERIAL_ORDER_MATERIAL_WITH_PRINTED));
                }
            }
        }

        /**
         * 発注日の検証.
         * <pre>
         * - 発注日が現在日より前の日付を指定されていないか検証
         * </pre>
         */
        private void validateOrderAt() {
            final Date orderAt = this.materialOrder.getOrderAt();

            // 発注日が過去日になっている場合はエラー
            if (orderAt.before(DateUtils.truncateDate(DateUtils.createNow()))) {
                this.resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_008).resource(RESOURCE_MATERIAL_ORDER_ORDER_AT));
                return;
            }
        }

        /**
         * 希望出荷日の検証.
         * <pre>
         * - 希望出荷日が発注日より前の日付を指定されていないか検証
         * - 希望出荷日が休業日を指定されていないか検証
         * - 希望出荷日が最短出荷可能日より前の日付を指定されていないか検証
         * </pre>
         */
        private void validatePreferredShippingAt() {
            // 発注日
            final Date orderAt = this.materialOrder.getOrderAt();
            // 希望出荷日
            final Date preferredShippingAt = this.materialOrder.getPreferredShippingAt();
            // 発注日と希望出荷日のどちらか遅い日
            final Date laterDate = DateUtils.whicheverComesLater(orderAt, preferredShippingAt);

            // 希望出荷日が発注日より前を指定している場合エラーとする
            if (preferredShippingAt.before(orderAt)) {
                this.resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_FO_009).resource(RESOURCE_MATERIAL_ORDER_PREFERRED_SHIPPING_AT));
                return;
            }
            // 緊急の出荷希望判定ありの場合、無条件で希望出荷日を正常とする
            if (this.materialOrder.getUrgent() == BooleanType.TRUE) {
                return;
            }

            // 受注カレンダを取得する（期間は発注日～発注日に1年算した日）
            final List<MCalendarEntity> receiveOrderCalenderList = mCalendarRepository.findByCalendarTypeAndSpecifyDateRange(CalendarType.FUKUI_JAPAN, orderAt,
                    DateUtils.add(orderAt, Calendar.YEAR, 1), PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("date")))).getContent();
            // 受注日を取得する
            final MCalendarEntity receiveOrderAtCalender = generatedShortestShippingDate(orderAt, SpcifyBusinessDay.LATEST_BUSINESS_DAY,
                    receiveOrderCalenderList);

            // 出荷日カレンダ種別を取得する
            CalendarType calendarType = CalendarType.FUKUI_JAPAN;
            if (this.materialOrder.getDeliveryType() == FukukitaruMasterDeliveryType.OVERSEES) {
                // デリバリが 海外 の場合、中国カレンダーを利用する
                calendarType = CalendarType.FUKUI_CHINA;
            }
            // 出荷日カレンダー情報を取得する（期間は発注日～発注日と希望出荷日のどちらか遅い日に1年加算した日）
            final List<MCalendarEntity> shipDateCalenderList = mCalendarRepository.findByCalendarTypeAndSpecifyDateRange(calendarType, orderAt,
                    DateUtils.add(laterDate, Calendar.YEAR, 1), PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("date")))).getContent();

            // 印字物有りのオーダーが1件でも入力されていれば「印字物含む」オーダーであると判定する
            final boolean isPrintedMaterial = CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuWashName())
                    || CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuAttentionName())
                    || CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuBottomBill())
                    || CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuAttentionTag())
                    || CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuBottomBillAttention())
                    || CollectionUtils.isNotEmpty(this.materialOrder.getOrderSkuBottomBillNergyMerit());

            // 希望出荷日のカレンダ情報を取得し、希望出荷日が休業日の場合エラーとする
            final CalendarType finalCalenderType = calendarType; // lamdaで利用するためだけのfinal変数
            final MCalendarEntity preferredShippingAtCalender = mCalendarRepository.findByCalendarTypeAndDate(calendarType, preferredShippingAt)
                    .orElseThrow(() -> new SystemException(MessageCodeType.SYSTEM_ERROR, LogStringUtil.of("isNonBusinessDay").message("m_calendar not found.")
                            .value("calendar_type", finalCalenderType).value("date", preferredShippingAt).build()));
            if (preferredShippingAtCalender.getBusinessDayFlg() == BusinessDayFlgType.HOLIDAY) {
                // 希望出荷日が休業日の場合エラー
                this.resultMessages.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_010).resource(RESOURCE_MATERIAL_ORDER_PREFERRED_SHIPPING_AT));
                return;
            }

            // 営業日を指定する（0:当日,1:翌営業日,2:翌々営業日）
            SpcifyBusinessDay specifybusinessDay = SpcifyBusinessDay.LATEST_BUSINESS_DAY;
            if (isPrintedMaterial && receiveOrderAtCalender.getBusinessDayFlg() == BusinessDayFlgType.BUSINESS_DAY) {
                // 印字物含む かつ 発注日が営業日の場合、翌営業日を設定する
                specifybusinessDay = SpcifyBusinessDay.NEXT_BUSINESS_DAY;
            } else if (isPrintedMaterial && receiveOrderAtCalender.getBusinessDayFlg() == BusinessDayFlgType.HOLIDAY) {
                // 印字物含む かつ 発注日が休業日の場合、翌々営業日を設定する
                specifybusinessDay = SpcifyBusinessDay.TWO_BUSINESS_DAYS_AFTER;
            } else if (!isPrintedMaterial && receiveOrderAtCalender.getBusinessDayFlg() == BusinessDayFlgType.BUSINESS_DAY) {
                // 印字物含まない かつ 発注日が営業日の場合、当日を設定する
                specifybusinessDay = SpcifyBusinessDay.LATEST_BUSINESS_DAY;
            } else if (!isPrintedMaterial && receiveOrderAtCalender.getBusinessDayFlg() == BusinessDayFlgType.HOLIDAY) {
                // 印字物含まない かつ 発注日が休業日の場合、翌営業日を設定する
                specifybusinessDay = SpcifyBusinessDay.NEXT_BUSINESS_DAY;
            }

            // 発注日を起点にした有効な直近営業日を取得する
            final MCalendarEntity businessDay = generatedShortestShippingDate(orderAt, specifybusinessDay, shipDateCalenderList);
            // 希望出荷日 が 最短出荷可能日より前の場合エラー
            if (preferredShippingAt.before(businessDay.getDate())) {
                this.resultMessages
                        .add(ResultMessage.fromCode(MessageCodeType.CODE_FO_011).resource(RESOURCE_MATERIAL_ORDER_PREFERRED_SHIPPING_AT));
                return;
            }

        }

        /**
         * 最短出荷日を生成. dateを起点に、specifybusinessDayに指定された日を取得する.
         *
         * @param date
         *            日付
         * @param specifybusinessDay
         *            指定営業日
         * @param calendars
         *            カレンダー
         * @return dateを起点に、specifybusinessDayに指定された日付
         */
        private MCalendarEntity generatedShortestShippingDate(final Date date, final SpcifyBusinessDay specifybusinessDay,
                final List<MCalendarEntity> calendars) {
            final Date wkDate;
            // 翌日日
            final Date nextDate = DateUtils.add(date, Calendar.DAY_OF_MONTH, 1);
            switch (specifybusinessDay) {
            case LATEST_BUSINESS_DAY:
                wkDate = date;
                break;
            case NEXT_BUSINESS_DAY:
                wkDate = nextDate;
                break;
            case TWO_BUSINESS_DAYS_AFTER:
                // 回帰処理を2回
                final MCalendarEntity wk = generatedShortestShippingDate(nextDate, SpcifyBusinessDay.NEXT_BUSINESS_DAY, calendars);
                return generatedShortestShippingDate(wk.getDate(), SpcifyBusinessDay.NEXT_BUSINESS_DAY, calendars);
            default:
                throw new SystemException(MessageCodeType.SYSTEM_ERROR,
                        LogStringUtil.of("generatedShortestShippingDate").message("specify business day not found.").build());
            }

            // カレンダーから該当する日付を抽出する
            final MCalendarEntity calendar = calendars.stream().filter(cal -> cal.getDate().compareTo(wkDate) == 0).findFirst().orElseThrow(
                    () -> new SystemException(MessageCodeType.SYSTEM_ERROR,
                            LogStringUtil.of("generatedShortestShippingDate").message("m_calendar not found.").build()));

            if (calendar.getBusinessDayFlg() == BusinessDayFlgType.BUSINESS_DAY) {
                // 営業日の場合、日付を返す
                return calendar;
            }
            // 休業日の場合、翌日で回帰処理する
            return generatedShortestShippingDate(nextDate, SpcifyBusinessDay.LATEST_BUSINESS_DAY, calendars);

        }

        /**
         * アテンション下札の検証.
         * <pre>
         * - アテンション下札が指定されている場合に付記用語が指定されていないか検証
         * </pre>
         */
        private void validateBottomBillAtention() {
            if (CollectionUtils.isEmpty(this.materialOrder.getOrderSkuBottomBillAttention())) {
                // アテンション下札がない場合検証しない
                return;
            }
            // アテンション下札を色ごとにチェックする
            this.materialOrder.getOrderSkuBottomBillAttention().stream().filter(row -> row.getOrderLot() != null).forEach(attentionValue -> {
                // アテンションタグ付記用語の00共通で設定されたコードの件数を取得
                long commonCount = this.materialOrder.getFkItem().getListItemAttentionAppendicesTerm().stream()
                        .filter(appendicesTermValue -> COMMON_COLOR_CODE.equals(appendicesTermValue.getColorCode())).count();

                // アテンションタグ付記用語の該当の色で設定されたコードの件数を取得
                long byColorCount = this.materialOrder.getFkItem().getListItemAttentionAppendicesTerm().stream()
                        .filter(appendicesTermValue -> appendicesTermValue.getColorCode().equals(attentionValue.getColorCode())).count();

                if ((commonCount <= 0) && (byColorCount <= 0)) {
                    // アテンション下札が指定されているが、付記用語が指定されていないためエラー
                    this.resultMessages
                            .add(ResultMessage.fromCode(MessageCodeType.CODE_FO_012).resource(RESOURCE_MATERIAL_ORDER_BOTTOM_BILLATENTION));
                    return;
                }
            });
        }
    }
}
