package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.schedule.ExtendedDistributionShipmentScheduleEntity;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.DistributionShipmentType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 配分出荷指示データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class DistributionShipmentLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<ExtendedDistributionShipmentScheduleEntity> {

    // サイズコード("00"固定)
    private static final String SIZE_CODE = "00";

    // 引取回数文字長
    private static final int DELIVERY_COUNT_LENGTH = 2;

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DISTRIBUTION_SHIPMENT_INSTRUCTION;
    }

    @Override
    void createRecord(final CSVPrinter printer, final ExtendedDistributionShipmentScheduleEntity entity) throws Exception {

        printer.printRecord(
                DateUtils.formatFromDate(entity.getManageDate(), "yyyyMMdd"), // 管理情報日付
                DateUtils.formatFromDate(entity.getManageAt(), "HHmmss"), // 管理情報時間
                entity.getManageNumber(), // 管理情報管理No
                generateIntegerToBigDecimal(entity.getLineNumber()), // 管理情報行No
                getBusinessType().getValue(), // データ種別("SH"固定)
                DistributionShipmentType.DISTRIBUTION.getValue(), // 出荷区分("1"固定)
                entity.getArrivalPlace(), // 入荷場所
                entity.getHjun(), // 配分順位
                entity.getSuspendType().getValue(), // 保留区分
                entity.getStoreCode(), // 店舗コード
                DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMdd"), // 出荷日
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                SIZE_CODE, // サイズコード("00"固定)
                entity.getSize(), // サイズ記号
                generateIntegerToBigDecimal(entity.getDeliveryLot()), // 出荷指示数
                entity.getRetailPrice(), // 上代
                entity.getPercent(), // 掛率
                entity.getUnderRetailPrice(), // 下代
                entity.getPsType().getValue(), // 品番情報 ＰＳ区分
                entity.getSaleRetailPrice(), // 品番情報 セール上代
                entity.getOffPercent(), // 品番情報 OFF%
                "", // 品別番号
                "", // 相手品番
                "", // 相手品名
                "", // 丸井発注No.
                entity.getOrderNumber(), // 仕入ＫＥＹ　発注番号
                StringUtils.leftPad(Integer.toString(entity.getDeliveryCount()), DELIVERY_COUNT_LENGTH, '0'), // 仕入ＫＥＹ　引取回数
                entity.getDivisionCode(), // 仕入ＫＥＹ　配分課
                ""
                );
    };
}
