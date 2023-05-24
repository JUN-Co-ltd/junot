package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.MakerReturnLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * メーカー返品確定データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class MakerReturnLinkingImportCsvFileComponent
extends LinkingImportCsvFileComponent<MakerReturnLinkingImportCsvModel> {

    /** 管理情報 日付. */
    private static final int NUMBER_0 = 0;

    /** 管理情報 時間. */
    private static final int NUMBER_1 = 1;

    /** 管理情報 管理No. */
    private static final int NUMBER_2 = 2;

    /** 管理情報 行No. */
    private static final int NUMBER_3 = 3;

    /** データ種別. */
    private static final int NUMBER_4 = 4;

    /** 入荷区分. */
    private static final int NUMBER_5 = 5;

    /** 入荷場所. */
    private static final int NUMBER_6 = 6;

    /** 店舗コード. */
    private static final int NUMBER_7 = 7;

    /** 返品日. */
    private static final int NUMBER_8 = 8;

    /** 伝票 No. */
    private static final int NUMBER_9 = 9;

    /** 伝票 行. */
    private static final int NUMBER_10 = 10;

    /** 店舗伝票 No. */
    private static final int NUMBER_11 = 11;

    /** 店舗伝票 行. */
    private static final int NUMBER_12 = 12;

    /** 品番. */
    private static final int NUMBER_13 = 13;

    /** カラー. */
    private static final int NUMBER_14 = 14;

    /** サイズコード. */
    private static final int NUMBER_15 = 15;

    /** 入荷予定数. */
    private static final int NUMBER_16 = 16;

    /** 入荷確定数. */
    private static final int NUMBER_17 = 17;

    /** 指示番号. */
    private static final int NUMBER_18 = 18;

    /** 指示番号 行. */
    private static final int NUMBER_19 = 19;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    MakerReturnLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final MakerReturnLinkingImportCsvModel model = new MakerReturnLinkingImportCsvModel();
        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setManageNumber(csvRecord.get(NUMBER_2));
        model.setLineNumber(csvRecord.get(NUMBER_3));
        model.setDataType(csvRecord.get(NUMBER_4));
        model.setArrivalType(csvRecord.get(NUMBER_5));
        model.setArrivalPlace(csvRecord.get(NUMBER_6));
        model.setStoreCode(csvRecord.get(NUMBER_7));
        model.setReturnAt(csvRecord.get(NUMBER_8));
        model.setVoucherNumber(csvRecord.get(NUMBER_9));
        model.setVoucherLine(csvRecord.get(NUMBER_10));
        model.setStoreVoucherNumber(csvRecord.get(NUMBER_11));
        model.setStoreVoucherLine(csvRecord.get(NUMBER_12));
        model.setPartNo(csvRecord.get(NUMBER_13));
        model.setColorCode(csvRecord.get(NUMBER_14));
        model.setSize(csvRecord.get(NUMBER_15));
        model.setArrivalCount(csvRecord.get(NUMBER_16));
        model.setFixArrivalCount(csvRecord.get(NUMBER_17));
        model.setInstructNumber(csvRecord.get(NUMBER_18));
        model.setInstructNumberLine(csvRecord.get(NUMBER_19));
        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.RETURN_CONFIRM.getValue();
    }
}
