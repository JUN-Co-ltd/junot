package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.PurchaseLinkingImportFromSqCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 仕入確定データ（SQ）の CSVファイルをダウンロードし、データを読み込むコンポーネント.
 * PRD_0071 add SIT
 */
@Component
public class PurchaseLinkingImportFromSqCsvFileComponent extends LinkingImportCsvFileComponent<PurchaseLinkingImportFromSqCsvModel> {

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

    /** 入荷店舗. */
    private static final int NUMBER_7 = 7;

    /** 仕入先. */
    private static final int NUMBER_8 = 8;

    /** 製品工場 */
    private static final int NUMBER_9 = 9;

    /** 入荷日. */
    private static final int NUMBER_10 = 10;

    /** 計上日. */
    private static final int NUMBER_11 = 11;

    /** 相手伝票No. */
    private static final int NUMBER_12 = 12;

    /** 仕入伝票No. */
    private static final int NUMBER_13 = 13;

    /** 仕入伝票行. */
    private static final int NUMBER_14 = 14;

    /** 品番. */
    private static final int NUMBER_15 = 15;

    /** カラー. */
    private static final int NUMBER_16 = 16;

    /** サイズ(サイズ記号). */
    private static final int NUMBER_17 = 17;

    /** 入荷数. */
    private static final int NUMBER_18 = 18;

    /** 入荷確定数. */
    private static final int NUMBER_19 = 19;

    /** 良品・不良品区分. */
    private static final int NUMBER_20 = 20;

    /** 指示番号. */
    private static final int NUMBER_21 = 21;

    /** 指示番号行. */
    private static final int NUMBER_22 = 22;

    /** 発注番号. */
    private static final int NUMBER_23 = 23;

    /** 引取回数. */
    private static final int NUMBER_24 = 24;

    /** 配分課. */
    private static final int NUMBER_25 = 25;

    /** 仕入単価. */
    private static final int NUMBER_26 = 26;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    PurchaseLinkingImportFromSqCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final PurchaseLinkingImportFromSqCsvModel model = new PurchaseLinkingImportFromSqCsvModel();

        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setManageNumber(csvRecord.get(NUMBER_2));
        model.setLineNumber(csvRecord.get(NUMBER_3));
        model.setDataType(csvRecord.get(NUMBER_4));
        model.setPurchaseType(csvRecord.get(NUMBER_5));
        model.setArrivalPlace(csvRecord.get(NUMBER_6));
        model.setArrivalShop(csvRecord.get(NUMBER_7));
        model.setSupplierCode(csvRecord.get(NUMBER_8));
        model.setMakerFactoryCode(csvRecord.get(NUMBER_9));
        model.setArrivalAt(csvRecord.get(NUMBER_10));
        model.setRecordAt(csvRecord.get(NUMBER_11));
        model.setMakerVoucherNumber(csvRecord.get(NUMBER_12));
        model.setPurchaseVoucherNumber(csvRecord.get(NUMBER_13));
        model.setPurchaseVoucherLine(csvRecord.get(NUMBER_14));
        model.setPartNo(csvRecord.get(NUMBER_15));
        model.setColorCode(csvRecord.get(NUMBER_16));
        model.setSize(csvRecord.get(NUMBER_17));
        model.setArrivalCount(csvRecord.get(NUMBER_18));
        model.setFixArrivalCount(csvRecord.get(NUMBER_19));
        model.setNonConformingProductType(csvRecord.get(NUMBER_20));
        model.setInstructNumber(csvRecord.get(NUMBER_21));
        model.setInstructNumberLine(csvRecord.get(NUMBER_22));
        model.setOrderId(csvRecord.get(NUMBER_23));
        model.setPurchaseCount(csvRecord.get(NUMBER_24));
        model.setDivisionCode(csvRecord.get(NUMBER_25));
        model.setPurchaseUnitPrice(csvRecord.get(NUMBER_26));
        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.PURCHASE_CONFIRM.getValue();
    }
}
