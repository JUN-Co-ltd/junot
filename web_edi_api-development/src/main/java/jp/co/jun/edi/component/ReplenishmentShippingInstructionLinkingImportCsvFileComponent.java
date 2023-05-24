package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.ReplenishmentShippingInstructionLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 補充出荷指示データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class ReplenishmentShippingInstructionLinkingImportCsvFileComponent
extends LinkingImportCsvFileComponent<ReplenishmentShippingInstructionLinkingImportCsvModel> {

    /** 管理情報 日付. */
    private static final int NUMBER_0 = 0;

    /** 管理情報 時間. */
    private static final int NUMBER_1 = 1;

    /** 管理情報 SEQ. */
    private static final int NUMBER_2 = 2;

    /** 出荷先店舗コード（移動先店舗）. */
    private static final int NUMBER_3 = 3;

    /** 保留先店舗コード. */
    private static final int NUMBER_4 = 4;

    /** 出荷日. */
    private static final int NUMBER_5 = 5;

    /** 出荷場所. */
    private static final int NUMBER_6 = 6;

    /** 品番. */
    private static final int NUMBER_7 = 7;

    /** カラー. */
    private static final int NUMBER_8 = 8;

    /** サイズ. */
    private static final int NUMBER_9 = 9;

    /** 出荷指示数. */
    private static final int NUMBER_10 = 10;

    /** データ種別. */
    private static final int NUMBER_11 = 11;

    /** 登録日. */
    private static final int NUMBER_12 = 12;

    /** 修正日. */
    private static final int NUMBER_13 = 13;

    /** 入力者. */
    private static final int NUMBER_14 = 14;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    ReplenishmentShippingInstructionLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final ReplenishmentShippingInstructionLinkingImportCsvModel model = new ReplenishmentShippingInstructionLinkingImportCsvModel();
        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setSequence(csvRecord.get(NUMBER_2));
        model.setShipmentShopCode(csvRecord.get(NUMBER_3));
        model.setHoldShopCode(csvRecord.get(NUMBER_4));
        model.setCargoAt(csvRecord.get(NUMBER_5));
        model.setCargoPlace(csvRecord.get(NUMBER_6));
        model.setPartNo(csvRecord.get(NUMBER_7));
        model.setColorCode(csvRecord.get(NUMBER_8));
        model.setSize(csvRecord.get(NUMBER_9));
        model.setShippingInstructionsLot(csvRecord.get(NUMBER_10));
        model.setDataType(csvRecord.get(NUMBER_11));
        model.setCreatedAt(csvRecord.get(NUMBER_12));
        model.setUpdatedAt(csvRecord.get(NUMBER_13));
        model.setTanto(csvRecord.get(NUMBER_14));
        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.REPLENISHMENT_SHIPPING_INSTRUCTION.getValue();
    }
}
