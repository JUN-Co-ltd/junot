package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.InventoryShipmentLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 在庫出荷確定データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class InventoryShipmentLinkingImportCsvFileComponent extends LinkingImportCsvFileComponent<InventoryShipmentLinkingImportCsvModel> {

    /** 管理情報日付. */
    private static final int NUMBER_0 = 0;

    /** 管理情報時間. */
    private static final int NUMBER_1 = 1;

    /** 管理情報管理No. */
    private static final int NUMBER_2 = 2;

    /** 管理情報行No. */
    private static final int NUMBER_3 = 3;

    /** データ種別. */
    private static final int NUMBER_4 = 4;

    /** 出荷区分. */
    private static final int NUMBER_5 = 5;

    /** 出荷場所. */
    private static final int NUMBER_6 = 6;

    /** 店舗コード. */
    private static final int NUMBER_7 = 7;

    /** 出荷日. */
    private static final int NUMBER_8 = 8;

    /** 出荷伝票 No. */
    private static final int NUMBER_9 = 9;

    /** 出荷伝票 行. */
    private static final int NUMBER_10 = 10;

    /** 品番. */
    private static final int NUMBER_11 = 11;

    /** カラー. */
    private static final int NUMBER_12 = 12;

    /** サイズコード. */
    private static final int NUMBER_13 = 13;

    /** 出荷指示数. */
    private static final int NUMBER_14 = 14;

    /** 出荷確定数. */
    private static final int NUMBER_15 = 15;

    /** 箱No.（梱包No.）. */
    private static final int NUMBER_16 = 16;

    /** 指示番号. */
    private static final int NUMBER_17 = 17;

    /** 指示番号行. */
    private static final int NUMBER_18 = 18;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    InventoryShipmentLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final InventoryShipmentLinkingImportCsvModel model = new InventoryShipmentLinkingImportCsvModel();

        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setManageNumber(csvRecord.get(NUMBER_2));
        model.setLineNumber(csvRecord.get(NUMBER_3));
        model.setDataType(csvRecord.get(NUMBER_4));
        model.setShipmentType(csvRecord.get(NUMBER_5));
        model.setShipmentPlace(csvRecord.get(NUMBER_6));
        model.setTnpCode(csvRecord.get(NUMBER_7));
        model.setShipmentAt(csvRecord.get(NUMBER_8));
        model.setShipmentVoucherNumber(csvRecord.get(NUMBER_9));
        model.setShipmentVoucherLine(csvRecord.get(NUMBER_10));
        model.setPartNo(csvRecord.get(NUMBER_11));
        model.setColorCode(csvRecord.get(NUMBER_12));
        model.setSize(csvRecord.get(NUMBER_13));
        model.setShippingInstructionLot(csvRecord.get(NUMBER_14));
        model.setFixShippingInstructionLot(csvRecord.get(NUMBER_15));
        model.setPackageNumber(csvRecord.get(NUMBER_16));
        model.setInstructNumber(csvRecord.get(NUMBER_17));
        model.setInstructNumberLine(csvRecord.get(NUMBER_18));

        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.INVENTORY_CONFIRM.getValue();
    }
}
