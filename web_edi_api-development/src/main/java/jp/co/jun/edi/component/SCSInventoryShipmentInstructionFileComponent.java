package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.InventoryShipmentImportCsvModel;
import jp.co.jun.edi.type.BusinessType;


/**
 * SCS・ZOZO在庫出荷指示データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class SCSInventoryShipmentInstructionFileComponent extends LinkingImportCsvFileComponent<InventoryShipmentImportCsvModel> {

    /** 管理情報 日付. */
    private static final int NUMBER_0 = 0;

    /** 管理情報 時間. */
    private static final int NUMBER_1 = 1;

    /** 管理情報 管理No. */
    private static final int NUMBER_2 = 2;

    /** 管理情報 行No. */
    private static final int NUMBER_3 = 3;

    /** 出荷区分. */
    private static final int NUMBER_5 = 5;

    /** 出荷場所. */
    private static final int NUMBER_6 = 6;

    /** 配分順位. */
    private static final int NUMBER_7 = 7;

    /** 店舗コード. */
    private static final int NUMBER_8 = 8;

    /** 出荷日. */
    private static final int NUMBER_9 = 9;

    /** 品番. */
    private static final int NUMBER_10 = 10;

    /** カラー. */
    private static final int NUMBER_11 = 11;

    /** サイズ記号. */
    private static final int NUMBER_13 = 13;

    /** 出荷指示数. */
    private static final int NUMBER_14 = 14;

    /** 上代. */
    private static final int NUMBER_15 = 15;

    /** 掛率. */
    private static final int NUMBER_16 = 16;

    /** 下代. */
    private static final int NUMBER_17 = 17;

    /** PS区分. */
    private static final int NUMBER_18 = 18;

    /** セール上代. */
    private static final int NUMBER_19 = 19;

    /** OFF%. */
    private static final int NUMBER_20 = 20;

    /** 指示管理_担当者. */
    private static final int NUMBER_25 = 25;

    /** 指示管理_日付. */
    private static final int NUMBER_26 = 26;

    /** 指示管理_時間. */
    private static final int NUMBER_27 = 27;

    /** 指示明細__指示番号. */
    private static final int NUMBER_28 = 28;

    /** 指示明細_店舗. */
    private static final int NUMBER_30 = 30;

    /** 指示明細_品番. */
    private static final int NUMBER_31 = 31;

    /** 指示明細_課. */
    private static final int NUMBER_32 = 32;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    InventoryShipmentImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final InventoryShipmentImportCsvModel model = new InventoryShipmentImportCsvModel();

        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setManageNumber(csvRecord.get(NUMBER_2));
        model.setSequence(csvRecord.get(NUMBER_3));
        model.setShippingCategory(csvRecord.get(NUMBER_5));
        model.setCargoPlace(csvRecord.get(NUMBER_6));
        model.setAllocationRank(csvRecord.get(NUMBER_7));
        model.setShopCode(csvRecord.get(NUMBER_8));
        model.setCargoAt(csvRecord.get(NUMBER_9));
        model.setPartNo(csvRecord.get(NUMBER_10));
        model.setColorCode(csvRecord.get(NUMBER_11));
        model.setSize(csvRecord.get(NUMBER_13));
        model.setShippingInstructionLot(csvRecord.get(NUMBER_14));
        model.setRetailPrice(csvRecord.get(NUMBER_15));
        model.setRate(csvRecord.get(NUMBER_16));
        model.setWholesalePrice(csvRecord.get(NUMBER_17));
        model.setProperSaleType(csvRecord.get(NUMBER_18));
        model.setSaleRetailPrice(csvRecord.get(NUMBER_19));
        model.setOffPercent(csvRecord.get(NUMBER_20));
        model.setInstructionManageUserCode(csvRecord.get(NUMBER_25));
        model.setInstructionManageUserDate(csvRecord.get(NUMBER_26));
        model.setInstructionManageUserAt(csvRecord.get(NUMBER_27));
        model.setInstructionManageNumber(csvRecord.get(NUMBER_28));
        model.setInstructionManageShopCode(csvRecord.get(NUMBER_30));
        model.setInstructionManagePartNo(csvRecord.get(NUMBER_31));
        model.setInstructionManageDivisionCode(csvRecord.get(NUMBER_32));

        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.INVENTORY_IMPORT.getValue();
    }
}
