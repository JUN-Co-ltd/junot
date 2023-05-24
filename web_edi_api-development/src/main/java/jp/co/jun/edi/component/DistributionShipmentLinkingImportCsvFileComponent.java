package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.DistributionShipmentLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 配分出荷指示確定データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class DistributionShipmentLinkingImportCsvFileComponent
extends LinkingImportCsvFileComponent<DistributionShipmentLinkingImportCsvModel> {

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

    /** 出荷区分. */
    private static final int NUMBER_5 = 5;

    /** 出荷場所. */
    private static final int NUMBER_6 = 6;

    /** 保留区分. */
    private static final int NUMBER_7 = 7;

    /** 店舗コード. */
    private static final int NUMBER_8 = 8;

    /** 出荷日. */
    private static final int NUMBER_9 = 9;

    /** 出荷伝票No. */
    private static final int NUMBER_10 = 10;

    /** 出荷伝票行. */
    private static final int NUMBER_11 = 11;

    /** 品番. */
    private static final int NUMBER_12 = 12;

    /** カラー. */
    private static final int NUMBER_13 = 13;

    /** サイズコード. */
    private static final int NUMBER_14 = 14;

    /** 出荷指示数. */
    private static final int NUMBER_15 = 15;

    /** 出荷確定数. */
    private static final int NUMBER_16 = 16;

    /** 箱No（梱包No）. */
    private static final int NUMBER_17 = 17;

    /** KEY 発注番号. */
    private static final int NUMBER_18 = 18;

    /** KEY 引取回数. */
    private static final int NUMBER_19 = 19;

    /** KEY 配分課. */
    private static final int NUMBER_20 = 20;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    DistributionShipmentLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final DistributionShipmentLinkingImportCsvModel model =
                new DistributionShipmentLinkingImportCsvModel();

        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setManageNumber(csvRecord.get(NUMBER_2));
        model.setLineNumber(csvRecord.get(NUMBER_3));
        model.setDataType(csvRecord.get(NUMBER_4));
        model.setShipmentType(csvRecord.get(NUMBER_5));
        model.setShipmentPlace(csvRecord.get(NUMBER_6));
        model.setSuspendType(csvRecord.get(NUMBER_7));
        model.setTnpCode(csvRecord.get(NUMBER_8));
        model.setShipmentAt(csvRecord.get(NUMBER_9));
        model.setShipmentVoucherNumber(csvRecord.get(NUMBER_10));
        model.setShipmentVoucherLine(csvRecord.get(NUMBER_11));
        model.setPartNo(csvRecord.get(NUMBER_12));
        model.setColorCode(csvRecord.get(NUMBER_13));
        model.setSizeCode(csvRecord.get(NUMBER_14));
        model.setAllocationLot(csvRecord.get(NUMBER_15));
        model.setAllocationFixLot(csvRecord.get(NUMBER_16));
        model.setPackageNumber(csvRecord.get(NUMBER_17));
        model.setOrderNumber(csvRecord.get(NUMBER_18));
        model.setDeliveryCount(csvRecord.get(NUMBER_19));
        model.setDivisionCode(csvRecord.get(NUMBER_20));

        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.DISTRIBUTION_SHIPMENT_CONFIRM.getValue();
    }
}
