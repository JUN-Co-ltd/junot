package jp.co.jun.edi.component;

import java.math.BigDecimal;

import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnLinkingCsvFileEntity;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.util.DateUtils;

/**
 * メーカー返品指示データの CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class MakerReturnLinkingCreateCsvFileComponent
extends LinkingCreateCsvFileComponent<ExtendedTMakerReturnLinkingCsvFileEntity> {

    /** BigDecimal0. */
    private static final BigDecimal ZERO = new BigDecimal("0");

    /** サイズコード. */
    private static final String SIZE_CODE = "00";

    /** 入荷場所接尾辞. */
    private static final String ARRIVAL_PLACE_SUFFIX = "B";

    // PRD_0089 add SIT start
    private static final Integer addInstructNumber = 400000;
    // PRD_0089 add SIT end

    @Override
    BusinessType getBusinessType() {
        // PRD_0089 mod SIT start
        //return BusinessType.RETURN_INSTRUCTION;
        return BusinessType.PURCHASE_INSTRUCTION;
        // PRD_0089 mod SIT end
    }

    @Override
    void createRecord(final CSVPrinter printer, final ExtendedTMakerReturnLinkingCsvFileEntity entity) throws Exception {
        // PRD_0089 mod SIT start
        //String arrivalPlace = generateSubString(entity.getLogisticsCode(), 0, 1);  // 入荷場所(先頭1桁のみ)
        String arrivalPlace = generateSubString(entity.getLogisticsCode(), 0, 1) + ARRIVAL_PLACE_SUFFIX;  // 入荷場所(先頭1桁のみ) + B
        // PRD_0089 mod SIT end
        BigDecimal unitPrice = entity.getUnitPrice(); // 単価;

        final BigDecimal nonConformingProductUnitPrice = entity.getNonConformingProductUnitPrice();
        if (nonConformingProductUnitPrice != null && ZERO.compareTo(nonConformingProductUnitPrice) == -1)  {
            // 発注情報.B級品単価 > 0
            // PRD_0089 del SIT start
            //arrivalPlace = arrivalPlace + ARRIVAL_PLACE_SUFFIX;
            // PRD_0089 del SIT start
            unitPrice = nonConformingProductUnitPrice;
        }

        printer.printRecord(
                DateUtils.formatFromDate(entity.getManageDate(), "yyyyMMdd"), // 管理情報 日付
                DateUtils.formatFromDate(entity.getManageAt(), "HHmmss"), // 管理情報 時間
                entity.getManageNumber(), // 管理情報 管理No
                entity.getSequence(), // 管理情報 行No
                getBusinessType().getValue(), // データ種別
                // PRD_0089 mod SIT start
                //ArrivalType.RETURN.getValue(), // 入荷区分
                //arrivalPlace, // 入荷場所
                //entity.getShpcd(), // 店舗コード(直送移庫入荷:保留店舗)
                //DateUtils.formatFromDate(entity.getReturnAt(), "yyyyMMdd"), // 返品日(出荷日)
                //entity.getVoucherNumber(), // 店舗伝票 No.(店舗返品明細 又は、直送出荷No.)
                //entity.getVoucherLine(), // 伝票番号 行
                //"1",    // 店舗伝票 枝番
                PurchaseType.RETURN_PURCHASE.getValue(), //仕入区分
                arrivalPlace, // 入荷場所
                entity.getSupplierCode(), // 仕入先コード
                entity.getMdfMakerFactoryCode(), //製品工場
                DateUtils.formatFromDate(entity.getReturnAt(), "yyyyMMdd"), //入荷日
                entity.getOrderNumber(), //発注番号No.
                entity.getVoucherNumber(), //仕入伝票No.
                entity.getVoucherLine(), //仕入伝票行
                // PRD_0089 mod SIT end
                entity.getPartNo(), // 品番
                entity.getColorCode(), // カラー
                SIZE_CODE, // サイズコード
                entity.getSize(), // サイズ記号
                // PRD_0089 mod SIT start
                //entity.getReturnLot(), // 入荷予定数
                //unitPrice, // 上代※単価をセットする
                //entity.getProperRate(), // プロパー掛率
                //unitPrice, // 下代※上代をセットする
                //entity.getInstructNumber(), // 指示番号
                entity.getReturnLot(), // 入荷数
                entity.getRetailPrice(), //上代
                unitPrice, //単価
                "1", //良品・不良品区分
                Integer.parseInt(entity.getInstructNumber()) + addInstructNumber, // 指示番号
                // PRD_0089 mod SIT end
                entity.getInstructNumberLine()  // 指示番号行
                );
    };
}
