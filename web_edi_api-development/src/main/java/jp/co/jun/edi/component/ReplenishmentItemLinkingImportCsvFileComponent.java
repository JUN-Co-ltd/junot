package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.ReplenishmentItemLinkingImportCsvModel;
import jp.co.jun.edi.type.BusinessType;

/**
 * 補充対象品番データの CSVファイルをダウンロードし、データを読み込むコンポーネント.
 */
@Component
public class ReplenishmentItemLinkingImportCsvFileComponent
extends LinkingImportCsvFileComponent<ReplenishmentItemLinkingImportCsvModel> {

    /** 管理情報 日付. */
    private static final int NUMBER_0 = 0;

    /** 管理情報 時間. */
    private static final int NUMBER_1 = 1;

    /** 管理情報 SEQ. */
    private static final int NUMBER_2 = 2;

    /** 品番. */
    private static final int NUMBER_3 = 3;

    /** 登録日. */
    private static final int NUMBER_4 = 4;

    /** 修正日. */
    private static final int NUMBER_5 = 5;

    /** 入力者. */
    private static final int NUMBER_6 = 6;

    @Autowired
    private PropertyComponent propertyComponent;

    @Override
    ReplenishmentItemLinkingImportCsvModel convertCsvDataToModel(final CSVRecord csvRecord) {
        final ReplenishmentItemLinkingImportCsvModel model = new ReplenishmentItemLinkingImportCsvModel();
        model.setManageDate(csvRecord.get(NUMBER_0));
        model.setManageAt(csvRecord.get(NUMBER_1));
        model.setSequence(csvRecord.get(NUMBER_2));
        model.setPartNo(csvRecord.get(NUMBER_3));
        model.setCreatedAt(csvRecord.get(NUMBER_4));
        model.setUpdatedAt(csvRecord.get(NUMBER_5));
        model.setTanto(csvRecord.get(NUMBER_6));
        return model;
    }

    @Override
    String generateTempDirectory() {
        return propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + BusinessType.REPLENISHMENT_ITEM.getValue();
    }
}
