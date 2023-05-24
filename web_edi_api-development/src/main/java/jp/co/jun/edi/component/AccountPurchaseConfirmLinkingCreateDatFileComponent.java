package jp.co.jun.edi.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.AccountPurchaseConfirmFileLinkingDatModel;
import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 会計仕入確定データの DATファイルを作成し、DATファイルやディレクトリを削除するコンポーネント.
 */
@Component
public class AccountPurchaseConfirmLinkingCreateDatFileComponent {

    @Autowired
    private PropertyComponent propertyComponent;

    // DAT設定
    private static final CSVFormat DAT_FORMAT =
            CSVFormat.EXCEL.withEscape('"').withQuoteMode(QuoteMode.NONE)
            .withRecordSeparator("\r\n").withNullString("");

    // DATファイル接頭辞：
    private static final String DAT_FILE_PREFIX = "SRADAT_AS";

    // DATファイル接尾辞：.dat
    private static final String DAT_FILE_SUFFIX = ".dat";

    /**
     * DATファイルを作成し、一時フォルダに出力する.
     *
     * @param datModels ファイル作成用データリスト
     * @param recordAt 計上日
     * @return 生成したDATファイル
     * @throws Exception IOException
     */
    public File createDatFile(
            final List<AccountPurchaseConfirmFileLinkingDatModel> datModels,
            final Date recordAt) throws Exception {
        // DATファイル一時保存パス取得：/opt/junot-api/tmp/shipment/OR
        final String tmpDirectory = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + getBusinessType().getValue();
        final Path datPath = Paths.get(tmpDirectory);

        // ファイル名：SRADAT_AS[dd].dat
        // [dd]：計上日の日の部分(yyyyMMddのddの部分)
        // 例）SRADAT_AS30.dat
        final String fileName = DAT_FILE_PREFIX + DateUtils.formatFromDate(recordAt, "dd") + DAT_FILE_SUFFIX;

        // DATファイルを作成し、一時ディレクトリへ出力
        final File instructionFile = new File(datPath.toFile(), fileName);
        createShipmentFileInfo(instructionFile, datModels);

        return instructionFile;
    }

    /**
     * 指示データファイルを削除する.
     * 一時保存ディレクトリ(/opt/junot-api/tmp/shipment/OR)内のデータを全て削除
     *
     * @throws Exception IOException
     */
    public void deleteFiles() throws Exception {
        final String tmpDirectory =
                propertyComponent.getBatchProperty().getShipmentProperty().getShipmentTmpDirectory()
                + getBusinessType().getValue();
        final File instructionDir = new File(tmpDirectory);
        FileUtils.cleanDirectory(instructionDir);
    }

    /**
     * ファイルを作成.
     *
     * @param file ファイル
     * @param datModels ファイル作成用データリスト
     * @throws Exception 例外
     */
    private void createShipmentFileInfo(
            final File file,
            final List<AccountPurchaseConfirmFileLinkingDatModel> datModels) throws Exception {
        try (Writer wt = new OutputStreamWriter(new FileOutputStream(file), CharsetsConstants.MS932);
                CSVPrinter printer = new CSVPrinter(wt, DAT_FORMAT);) {
            for (AccountPurchaseConfirmFileLinkingDatModel dat : datModels) {
                // ボディ行出力
                printer.printRecord(createLineRecord(dat));
            }
        }
    }

    /**
     * 業務区分を返す.
     *
     * @return 業務区分
     */
    private BusinessType getBusinessType() {
        return BusinessType.ACCOUNT_PURCHASE_CONFIRM;
    }

    /**
     * 1行のレコード作成.
     *
     * @param datModel ファイル作成用Model
     * @return 連結後の文字列
     */
    private String createLineRecord(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        final StringBuilder sbf = new StringBuilder();

        sbf.append(datModel.getDataType()) // データ種別
        .append(datModel.getSqManageNumber()) // 入荷伝票No
        .append(datModel.getPurchaseVoucherLine()) // 入荷伝票行
        .append(datModel.getDeptCode()) // 部門
        .append(datModel.getArrivalPlaceFrom()) // 場所（自）
        .append(datModel.getArrivalPlaceTo()) // 場所（至）
        .append(datModel.getArrivalPlaceOccur()) // 場所（発生）
        .append(datModel.getDivisionCode()) // 配分コード 課
        .append(datModel.getAllocationCodeGroup()) // 配分コード グループ
        .append(datModel.getAllocationCodeRank()) // 配分コード 順位
        .append(datModel.getSupplierCode()) // 仕入先コード
        .append(datModel.getArrivalAt()) // 入荷日
        .append(datModel.getVoucherAt()) // 伝票日付
        .append(datModel.getRecordAt()) // 計上日
        .append(datModel.getVoucherCategory()) // 伝区(伝票区分)
        .append(datModel.getReceiptAndShipmentType()) // 入出荷区分
        .append(datModel.getVoucherVariety()) // 伝種(伝票種類)
        .append(datModel.getCorrectType()) // 訂正区分
        .append(datModel.getProductType()) // 製品区分
        .append(datModel.getCommonType1()) // 共通区分１
        .append(datModel.getCommonType2()) // 共通区分２
        .append(datModel.getCompleteType()) // 完納区分
        .append(datModel.getSetProductType()) // セット品区分
        .append(datModel.getNonConformingProductType()) // Ｂ級品区分
        .append(datModel.getPurchaseType1()) // 仕入区分１
        .append(datModel.getPurchaseType2()) // 仕入区分２
        .append(datModel.getType1()) // 区分１
        .append(datModel.getType2()) // 区分２
        .append(datModel.getOrderNumber()) // 発注No
        .append(datModel.getPurchaseCount()) // 引取回数
        .append(datModel.getExpenseItem()) // 費目
        .append(datModel.getPurchaseVoucherNumber()) // 仕入伝票No
        .append(datModel.getMakerVoucherNumber()) // 先方伝票No
        .append(datModel.getInvoiceNumber()) // 送り状No
        .append(datModel.getShippingCompanyCode()) // 運送会社コード
        .append(datModel.getPiece()) // 個数口
        .append(datModel.getHangerAmount()) // ハンガー数
        .append(datModel.getPartNo()) // 品番
        .append(datModel.getProductNameKana()) // 品名
        .append(datModel.getRetailPrice()) // 上代
        .append(datModel.getUnitPrice()) // 単価
        .append(datModel.getProductPrice()) // 金額
        .append(datModel.getFixArrivalCount()) // 数量
        .append(datModel.getColorCode()) // カラー
        .append(datModel.getSizeType()) // サイズ区分
        .append(datModel.getSize1()) // サイズ別数１
        .append(datModel.getSize2()) // サイズ別数２
        .append(datModel.getSize3()) // サイズ別数３
        .append(datModel.getSize4()) // サイズ別数４
        .append(datModel.getSize5()) // サイズ別数５
        .append(datModel.getSize6()) // サイズ別数６
        .append(datModel.getSize7()) // サイズ別数７
        .append(datModel.getSize8()) // サイズ別数８
        .append(datModel.getYear()) // 年度
        .append(datModel.getSeasonCode()) // シーズン
        .append(datModel.getMatlCost()) // 製造原価 生地
        .append(datModel.getProcessingCost()) // 製造原価 工賃
        .append(datModel.getAccessoriesCost()) // 製造原価 附属品
        .append(datModel.getOtherCost()) // 製造原価 その他
        .append(datModel.getManufacturingCostSum()) // 製造原価 合計
        .append(datModel.getOriginalFabricClothNumber()) // 原反 反番号
        .append(datModel.getOriginalFabricClothCount()) // 原反 反数
        .append(datModel.getOriginalFabricMeter()) // 原反 メーター数
        .append(datModel.getDeliveryRequestNumber()) // 納品依頼No
        .append(datModel.getReserve5Digit()) // 予備：5桁
        .append(datModel.getFileIdentification()) // ファイル識別
        .append(datModel.getDataSpecificDate()) // データ固有 日付
        .append(datModel.getDataSpecificType()) // データ固有 区分
        .append(datModel.getDataSpecificVoucherNumber()) // データ固有 伝票No
        .append(datModel.getDataSpecificLineNumber()) // データ固有 行No
        .append(datModel.getIfType()) // 会社 ＩＦ区分
        .append(datModel.getCompanyCode()) // 会社 コード
        .append(datModel.getReserve13Digit()) // 予備：13桁
        .append(datModel.getManageMntflg()) // 管理情報 メンテ区分
        .append(datModel.getManageStaff()) // 管理情報 担当者
        .append(datModel.getManageWsNumber()) // 管理情報 WSNo
        .append(datModel.getManageDate()) // 管理情報 日付
        .append(datModel.getManageAt()) // 管理情報 時間
        .append(datModel.getManageSeq()) // 管理情報 SEQ
        .append(datModel.getManageProgramName()) // 管理情報 プログラム名
        .append(datModel.getManageMedium()); // 管理情報 媒体

        return sbf.toString();
    }
}
