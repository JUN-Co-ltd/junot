package jp.co.jun.edi.component;

import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.csv.PurchaseConfirmCsvFileEntity;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 直送仕入確定データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class DirectPurchaseConfirmLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<PurchaseConfirmCsvFileEntity> {

    @Override
    BusinessType getBusinessType() {
        return BusinessType.DIRECT_PURCHASE_CONFIRM;
    }

    /**
     * 行Noの値がNULLの場合は、NULLをそれ以外は何も設定しない.
     * @param lineNumber 出荷場所
     * @return lineNumber
     */
    public Integer getLineNumber(final Integer lineNumber) {

        if (lineNumber == null) {
            return null;
        }
        return lineNumber;
    }

    /**
     * 良品・不用品区分がTRUEの場合"1"、それ以外は"0"に変換.
     * @param nonConformingProductType 良品・不用品区分
     * @return value
     */
    public String convertNonConformingProductType(final BooleanType nonConformingProductType) {
        if (nonConformingProductType == BooleanType.TRUE) {
            return "1";
        }
        return "0";
    }

    @Override
    void createRecord(final CSVPrinter printer, final PurchaseConfirmCsvFileEntity entity) throws Exception {
        printer.printRecord(
                DateUtils.formatFromDate(entity.getSqManageDate(), "yyyyMMdd"), // 管理情報日付
                DateUtils.formatFromDate(entity.getSqManageAt(), "HHmmss"), // 管理情報時間
                entity.getSqManageNumber(), // 管理情報管理No
                getLineNumber(entity.getLineNumber()), // 管理情報行No
                BusinessType.PURCHASE_CONFIRM.getValue(), // データ種別("KR"固定)
                entity.getPurchaseType().getValue(), // 仕入区分
                entity.getArrivalPlace(),  // 入荷場所
                DateUtils.formatFromDate(entity.getArrivalAt(), "yyyyMMdd"), // 入荷日
                entity.getPurchaseVoucherNumber(), // 仕入伝票No
                entity.getPurchaseVoucherLine(), // 仕入伝票行
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                entity.getSize(), // サイズ
                entity.getArrivalCount(), // 入荷数
                entity.getFixArrivalCount(), // 入荷確定数
                convertNonConformingProductType(entity.getNonConformingProductType()), // 良品・不用品区分
                entity.getInstructNumber(), // 指示番号
                entity.getInstructNumberLine() // 指示番号行
        );
    }
}
