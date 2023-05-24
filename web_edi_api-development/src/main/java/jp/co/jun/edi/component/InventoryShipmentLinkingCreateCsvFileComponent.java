package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.schedule.ExtendedInventoryShipmentScheduleEntity;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 在庫出荷指示データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class InventoryShipmentLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<ExtendedInventoryShipmentScheduleEntity> {

    // サイズコード("00"固定)
    private static final String SIZE_CODE = "00";

    // B級品区分設定
    private static final String NON_CONFORMING_PRODUCT_TYPE = "B";

    @Override
    BusinessType getBusinessType() {
        return BusinessType.INVENTORY_INSTRUCTION;
    }

    /**
     * Ｂ品区分の値がtrueの場合は、"B"をそれ以外はブランクを設定する.
     * @param cargoPlace 出荷場所
     * @param nonConformingProductType B級品区分
     * @return value
     */
    public String getcargoPlace(final String cargoPlace, final BooleanType nonConformingProductType) {

        final String value;

        if (nonConformingProductType == BooleanType.TRUE) {
            value = cargoPlace + NON_CONFORMING_PRODUCT_TYPE;
        } else {
            value = cargoPlace + "";
        }

        return String.valueOf(value);
    }

    @Override
    void createRecord(final CSVPrinter printer, final ExtendedInventoryShipmentScheduleEntity entity) throws Exception {
        printer.printRecord(
                DateUtils.formatFromDate(entity.getManageDate(), "yyyyMMdd"), // 管理情報日付
                DateUtils.formatFromDate(entity.getManageAt(), "HHmmss"), // 管理情報時間
                entity.getManageNumber(), // 管理情報管理No
                entity.getSequence(), // 管理情報行No
                getBusinessType().getValue(), // データ種別("SZ"固定)
                entity.getShippingCategory().getValue(), // 出荷区分
                getcargoPlace(entity.getCargoPlace(), entity.getNonConformingProductType()), // 出荷場所
                entity.getAllocationRank(), // 配分順位
                entity.getShopCode(), // 店舗コード
                DateUtils.formatFromDate(entity.getCargoAt(), "yyyyMMdd"), // 出荷日
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                SIZE_CODE, // サイズコード("00"固定)
                entity.getSize(), // サイズ記号
                entity.getShippingInstructionLot(), // 出荷指示数
                entity.getRetailPrice(), // 上代
                entity.getRate(), // 掛率
                entity.getWholesalePrice(), // 下代
                entity.getPsType().getValue(), // PS区分
                entity.getSaleRetailPrice(), // セール上代
                entity.getOffPercent(), // OFF%
                "", // 品別番号
                "", // 相手品番
                "", // 相手品名
                "", // 丸井発注No
                entity.getInstructionManageUserCode(), // 指示管理_担当者
                DateUtils.formatFromDate(entity.getInstructionManageUserDate(), "yyyyMMdd"), // 指示管理_日付
                DateUtils.formatFromDate(entity.getInstructionManageUserAt(), "HHmmss"), // 指示管理_時間
                entity.getInstructionManageNumber(), // 指示明細_指示番号
                entity.getInstructionManageNumberLine(), // 指示明細 指示番号行
                entity.getInstructionManageShopCode(), // 指示明細_店舗
                entity.getInstructionManagePartNo(), // 指示明細_品番
                entity.getInstructionManageDivisionCode(), // 指示明細_課
                ""
        );
    }
}
