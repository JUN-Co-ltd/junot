package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.schedule.ExtendedTPurchaseLinkingCreateCsvFileEntity;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 仕入指示データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class PurchaseLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<ExtendedTPurchaseLinkingCreateCsvFileEntity> {
    // サイズコード("00"固定)
    private static final String SIZE_CODE = "00";

    @Override
    BusinessType getBusinessType() {
        return BusinessType.PURCHASE_INSTRUCTION;
    }

    @Override
    void createRecord(final CSVPrinter printer, final ExtendedTPurchaseLinkingCreateCsvFileEntity entity) throws Exception {
        printer.printRecord(
                DateUtils.formatFromDate(entity.getSqManageDate(), "yyyyMMdd"), // 管理情報日付
                DateUtils.formatFromDate(entity.getSqManageAt(), "HHmmss"), // 管理情報時間
                entity.getSqManageNumber(), // 管理情報管理No
                generateIntegerToBigDecimal(entity.getLineNumber()), // 管理情報行No
                getBusinessType().getValue(), // データ種別("SR"固定)
                PurchaseType.ADDITIONAL_PURCHASE.getValue(), // 仕入区分("1"固定)
                generateSubString(entity.getArrivalPlace(), 0, 1), // 入荷場所(先頭1桁のみ)
                entity.getSupplierCode(), // 仕入先コード
                entity.getMdfMakerFactoryCode(), // 製品工場
                DateUtils.formatFromDate(entity.getArrivalAt(), "yyyyMMdd"), // 入荷日
                entity.getOrderNumber(), // 発注番号No
                entity.getPurchaseVoucherNumber(), // 仕入伝票No
                entity.getPurchaseVoucherLine(), // 仕入伝票行
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                SIZE_CODE, // サイズコード("00"固定)
                entity.getSize(), // サイズ記号
                generateIntegerToBigDecimal(entity.getArrivalCount()), // 入荷数
                entity.getRetailPrice(), // 上代
                entity.getUnitPrice(), // 単価
                generateBooleanTypeToString(entity.getNonConformingProductType()), // 良品・不良品区分
                entity.getInstructNumber(), // 指示番号
                generateIntegerToBigDecimal(entity.getInstructNumberLine()) // 指示番号行
                );
    };
}
