package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.PurchaseLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 仕入確定データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class PurchaseLinkingImportCsvFileComponent extends LinkingImportCsvFileComponent<PurchaseLinkingImportCsvModel> {

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

    /** 仕入区分. */
    private static final int NUMBER_5 = 5;

    /** 入荷場所. */
    private static final int NUMBER_6 = 6;

    /** 入荷日. */
    private static final int NUMBER_7 = 7;

    /** 仕入伝票No. */
    private static final int NUMBER_8 = 8;

    /** 仕入伝票行. */
    private static final int NUMBER_9 = 9;

    /** 品番. */
    private static final int NUMBER_10 = 10;

    /** カラー. */
    private static final int NUMBER_11 = 11;

    /** サイズコード. */
    private static final int NUMBER_12 = 12;

    /** 入荷数. */
    private static final int NUMBER_13 = 13;

    /** 入荷確定数. */
    private static final int NUMBER_14 = 14;

    /** 良品・不良品区分. */
    private static final int NUMBER_15 = 15;

    /** 指示番号. */
    private static final int NUMBER_16 = 16;

    /** 指示番号行. */
    private static final int NUMBER_17 = 17;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    PurchaseLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final PurchaseLinkingImportCsvModel model = new PurchaseLinkingImportCsvModel();

            model.setManageDate(csvRecord.get(NUMBER_0));
            model.setManageAt(csvRecord.get(NUMBER_1));
            model.setManageNumber(csvRecord.get(NUMBER_2));
            model.setLineNumber(csvRecord.get(NUMBER_3));
            model.setDataType(csvRecord.get(NUMBER_4));
            model.setPurchaseType(csvRecord.get(NUMBER_5));
            model.setArrivalPlace(csvRecord.get(NUMBER_6));
            model.setArrivalAt(csvRecord.get(NUMBER_7));
            model.setPurchaseVoucherNumber(csvRecord.get(NUMBER_8));
            model.setPurchaseVoucherLine(csvRecord.get(NUMBER_9));
            model.setPartNo(csvRecord.get(NUMBER_10));
            model.setColorCode(csvRecord.get(NUMBER_11));
            model.setSize(csvRecord.get(NUMBER_12));
            model.setArrivalCount(csvRecord.get(NUMBER_13));
            model.setFixArrivalCount(csvRecord.get(NUMBER_14));
            model.setNonConformingProductType(csvRecord.get(NUMBER_15));
            model.setInstructNumber(csvRecord.get(NUMBER_16));
            model.setInstructNumberLine(csvRecord.get(NUMBER_17));

        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.PURCHASE_CONFIRM.getValue();
    }
}
