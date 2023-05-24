package jp.co.jun.edi.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.FukukitaruOrderInfoModel;
import jp.co.jun.edi.entity.TFLinkingFileEntity;
import jp.co.jun.edi.entity.TSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTCompositionLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFAttentionAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFOrderSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashAppendicesTermEntity;
import jp.co.jun.edi.entity.extended.ExtendedTFWashPatternEntity;
import jp.co.jun.edi.repository.TFLinkingFileRepository;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * フクキタル連携 CSVファイルを作成し、CSVファイルやディレクトリを削除するコンポーネント.
 */
@Component
@Slf4j
public class FukukitaruLinkingCreateCsvFileComponent {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    // CSV設定
    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withQuoteMode(QuoteMode.ALL).withQuote('"').withRecordSeparator("\n").withNullString("");

    private static final Charset CHAR_SET = StandardCharsets.UTF_8;

    private static final String CONTENT_TYPE = "text/csv";

    // 共通のカラーコード：00
    private static final String COMMON_COLOR_CODE = "00";

    /** オーダー情報ファイル：接頭辞. */
    private static final String TMP_FILE_PREFIX_ORDER_INFO = "order_";

    /** オーダー情報ファイル：接尾辞. */
    private static final String TMP_FILE_SUFFIX_ORDER_INFO = ".csv";

    /** 品番情報ファイル：接頭辞. */
    private static final String TMP_FILE_PREFIX_ITEM_INFO = "item_";

    /** 品番情報ファイル：接尾辞. */
    private static final String TMP_FILE_SUFFIX_ITEM_INFO = ".csv";

    @Autowired
    private S3Component s3Component;

    @Autowired
    private TFLinkingFileRepository tFLinkingFileRepository;

    @Autowired
    private PropertyComponent propertyComponent;

    /**
     * CSVファイルを作成する.
     *
     * @param userId
     *            ユーザID
     * @param fukukitaruOrderInfoModel
     *            フクキタル発注情報モデル
     * @return 生成したファイルのリスト
     * @throws Exception
     *             IOException
     */
    public List<File> createCsvFile(final BigInteger userId, final FukukitaruOrderInfoModel fukukitaruOrderInfoModel) throws Exception {

        final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity = fukukitaruOrderInfoModel.getLinkingOrderInfoEntity();

        final List<File> createFiles = new ArrayList<File>();

        // オーダー識別コード単位のフォルダ作成
        final Path dirPath = getOrderDirectoryPath(linkingOrderInfoEntity.getOrderCode());
        Files.createDirectories(dirPath);

        // 発注CSVファイルを作成
        final File fileOrderInfo = new File(dirPath.toFile(),
                TMP_FILE_PREFIX_ORDER_INFO + DateUtils.formatFromDate(linkingOrderInfoEntity.getOrderAt(), "yyyyMMdd") + TMP_FILE_SUFFIX_ORDER_INFO);
        createOrderInfo(fileOrderInfo, fukukitaruOrderInfoModel);
        createFiles.add(fileOrderInfo);

        // 品番CSVファイルを作成
        final File fileItemInfo = new File(dirPath.toFile(),
                TMP_FILE_PREFIX_ITEM_INFO + DateUtils.formatFromDate(linkingOrderInfoEntity.getOrderAt(), "yyyyMMdd") + TMP_FILE_SUFFIX_ITEM_INFO);
        createItemInfo(fileItemInfo, fukukitaruOrderInfoModel);
        createFiles.add(fileItemInfo);

        // S3アップロード
        final String s3Prefix = propertyComponent.getBatchProperty().getMaterialOrderLinking().getS3Prefix();
        final String fileNameOrderInfo = TMP_FILE_PREFIX_ORDER_INFO + "_" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + TMP_FILE_SUFFIX_ORDER_INFO;
        final String s3KeyOrderInfo = s3Component.upload(fileOrderInfo, s3Prefix, CONTENT_TYPE);

        final String fileNameItemInfo = TMP_FILE_PREFIX_ITEM_INFO + "_" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + TMP_FILE_SUFFIX_ITEM_INFO;
        final String s3KeyItemInfo = s3Component.upload(fileItemInfo, s3Prefix, CONTENT_TYPE);

        // ファイルテーブル登録
        registTFLinkingFileEntity(linkingOrderInfoEntity.getId(), fileNameOrderInfo, s3KeyOrderInfo, userId);
        registTFLinkingFileEntity(linkingOrderInfoEntity.getId(), fileNameItemInfo, s3KeyItemInfo, userId);

        return createFiles;
    }

    /**
     * ファイルを削除し、ディレクトリも削除する.
     *
     * @param orderCode
     *            オーダー識別コード
     * @param files
     *            発注ファイル
     * @throws IOException
     *             IOException
     */
    public void deleteFilesAndDirectory(final String orderCode, final List<File> files) throws IOException {
        for (final File file : files) {
            // ファイルを削除
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    log.error("fukukitaru linking attachement temporary filedelete error.");
                }
            }
        }

        // オーダー識別コード単位の一時ディレクトリのパスを取得し、ディレクトリを削除する
        Files.delete(getOrderDirectoryPath(orderCode));
    }

    /**
     * 発注別の一時ディレクトリを取得する.
     *
     * @param orderCode
     *            オーダー識別コード
     * @return 発注別の一時ディレクトリ
     */
    private Path getOrderDirectoryPath(final String orderCode) {
        return Paths.get(propertyComponent.getBatchProperty().getMaterialOrderLinking().getTmpDirectory() + orderCode);
    }

    /**
     * 発注情報ファイルを作成.
     *
     * @param file
     *            ファイル
     * @param fukukitaruOrderInfoModel
     *            発注情報モデル
     * @throws IOException
     *             IOException
     */
    private void createOrderInfo(final File file, final FukukitaruOrderInfoModel fukukitaruOrderInfoModel) throws IOException {
        try (Writer wt = new OutputStreamWriter(new FileOutputStream(file), CHAR_SET); CSVPrinter printer = new CSVPrinter(wt, CSV_FORMAT);) {

            // ヘッダー行
            printOrderInfoHeader(printer);
            // ボディ行
            printOrderInfoBody(printer, fukukitaruOrderInfoModel);

        }
    }

    /**
     * 発注情報情報ヘッダー行を出力する.
     *
     * @param printer
     *            PrintWriter
     * @throws IOException
     *             IOException
     */
    private void printOrderInfoHeader(final CSVPrinter printer) throws IOException {
        printer.printRecord("オーダー識別コード", "発注日", "発注者コード", "請求先コード", "納入先コード", "納入先担当者", "緊急", "希望出荷日", "契約No.", "特記事項", "手配先", "リピート数", "工場No.", "製品品番",
                "アイテム名", "カラー", "サイズ", "資材種類", "資材コード", "数量");
    }

    /**
     * 発注情報ボディ行を出力する.
     *
     * @param printer
     *            PrintWriter
     * @param fukukitaruOrderInfoModel
     *            フクキタル発注情報モデル
     * @throws IOException
     *             IOException
     */
    private void printOrderInfoBody(final CSVPrinter printer, final FukukitaruOrderInfoModel fukukitaruOrderInfoModel) throws IOException {
        final List<ExtendedTFOrderSkuEntity> list = new ArrayList<>();

        // カラーコード共通(00)が指定されていない資材情報
        // 洗濯ネーム(1).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.WASH_NAME));
        // 洗濯同封副資材(3).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL));
        // 下札(4).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.HANG_TAG));
        // アテンション下札(6).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.ATTENTION_HANG_TAG));
        // NERGY用メリット下札(7).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.HANG_TAG_NERGY_MERIT));
        // 下札同封副資材(8).
        list.addAll(printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL));

        // カラーコード共通(00)が指定されている資材情報
        // アテンションネーム(2).
        list.addAll(printOrderInfoBodyByColor(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.ATTENTION_NAME));
        // アテンションタグ(5).
        list.addAll(printOrderInfoBodyByColor(fukukitaruOrderInfoModel, FukukitaruMasterMaterialType.ATTENTION_TAG));

        final ExtendedTFOrderLinkingEntity linkingOrderInfoEntity = fukukitaruOrderInfoModel.getLinkingOrderInfoEntity();

        // ボディ行出力
        for (ExtendedTFOrderSkuEntity extendedTFOrderSkuEntity : list) {
            printer.printRecord(linkingOrderInfoEntity.getOrderCode(), // オーダー識別コード
                    DateUtils.formatFromDate(linkingOrderInfoEntity.getOrderAt(), "yyyy/M/d"), // 発注日
                    linkingOrderInfoEntity.getOrderUserId(), // 発注者コード
                    linkingOrderInfoEntity.getBillingCompanyId(), // 請求先コード
                    linkingOrderInfoEntity.getDeliveryCompanyId(), // 納入先コード
                    linkingOrderInfoEntity.getDeliveryStaff(), // 納入先担当者
                    linkingOrderInfoEntity.getUrgent(), // 緊急
                    DateUtils.formatFromDate(linkingOrderInfoEntity.getPreferredShippingAt(), "yyyy/M/d"), // 希望出荷日
                    linkingOrderInfoEntity.getContractNumber(), // 契約No.
                    linkingOrderInfoEntity.getSpecialReport(), // 特記事項
                    linkingOrderInfoEntity.getDeliveryType(), // 手配先
                    linkingOrderInfoEntity.getRepeatNumber(), // リピート数
                    linkingOrderInfoEntity.getMdfMakerFactoryCode(), // 工場No.
                    BusinessUtils.formatPartNo(linkingOrderInfoEntity.getPartNo()), // 製品品番
                    "", // アイテム名
                    extendedTFOrderSkuEntity.getColorCode(), // カラー
                    extendedTFOrderSkuEntity.getSize(), // サイズ
                    extendedTFOrderSkuEntity.getMaterialTypeName(), // 資材種類
                    extendedTFOrderSkuEntity.getMaterialCode(), // 資材コード
                    extendedTFOrderSkuEntity.getOrderLot() // 数量
            );
        }
    }

    /**
     * materialTypeで指定した資材情報をフィルタリングし、品番SKUのカラーコードに該当する資材情報を取得する.
     *
     * @param fukukitaruOrderInfoModel
     *            FukukitaruOrderInfoModel
     * @param materialType
     *            資材種別
     * @return 資材情報
     * @throws IOException
     *             例外
     */
    private List<ExtendedTFOrderSkuEntity> printOrderInfoBodyByColor(final FukukitaruOrderInfoModel fukukitaruOrderInfoModel,
            final FukukitaruMasterMaterialType materialType) throws IOException {
        // materialTypeで指定された資材情報のみ取得する
        final List<ExtendedTFOrderSkuEntity> listExtendedTFOrderSkuEntity = printOrderInfoBodyFillterMaterialType(fukukitaruOrderInfoModel, materialType);

        // 品番SKUリストから、重複しないカラーコードリストを取得する(共通(00)は含まない)
        final List<String> colorCodeList = getUniqueColorList(fukukitaruOrderInfoModel.getListTSkuEntity());

        // カラー指定で個別に設定されている資材情報を取得する(共通(00)の資材情報は含まない)
        final List<ExtendedTFOrderSkuEntity> colorList = new ArrayList<>();
        for (String colorCode : colorCodeList) {
            colorList.addAll(filterMaterial(colorCode, listExtendedTFOrderSkuEntity));
        }
        return colorList;
    }

    /**
     * materialTypeで指定した資材情報をフィルタリングする.
     *
     * @param fukukitaruOrderInfoModel
     *            FukukitaruOrderInfoModel
     * @param materialType
     *            資材種別
     * @return 資材情報
     * @throws IOException
     *             例外
     */
    private List<ExtendedTFOrderSkuEntity> printOrderInfoBodyFillterMaterialType(final FukukitaruOrderInfoModel fukukitaruOrderInfoModel,
            final FukukitaruMasterMaterialType materialType) throws IOException {
        // materialTypeで指定された資材情報のみ取得する
        return fukukitaruOrderInfoModel.getListExtendedTFOrderSkuEntity().stream().filter(entity -> {
            final FukukitaruMasterMaterialType type = FukukitaruMasterMaterialType.convertToType(entity.getMaterialType());
            if (materialType == type) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * 品番情報ファイルを作成.
     *
     * @param file
     *            ファイル
     * @param fukukitaruOrderInfoModel
     *            発注情報モデル
     * @throws IOException
     *             IOException
     */
    private void createItemInfo(final File file, final FukukitaruOrderInfoModel fukukitaruOrderInfoModel) throws IOException {
        try (Writer wt = new OutputStreamWriter(new FileOutputStream(file), CHAR_SET); CSVPrinter printer = new CSVPrinter(wt, CSV_FORMAT);) {

            // ヘッダー行
            printItemInfoHeader(printer);
            // ボディ行
            printItemInfoBody(printer, fukukitaruOrderInfoModel);

        }
    }

    /**
     * 品番情報ヘッダー行を出力する.
     *
     * @param printer
     *            PrintWriter
     * @throws IOException
     *             IOException
     */
    private void printItemInfoHeader(final CSVPrinter printer) throws IOException {
        printer.printRecord("オーダー識別コード", "ブランド名", "ブランド名記号", "REEFUR用ブランド", "製品品番", "季別", "カテゴリコード", "サタデーズサーフ用NY品番", "原産国", "上代", "アイテム名", "アイテム種類", "カラー",
                "サイズ", "洗濯ネームテープ種類", "洗濯ネームテープ巾", "洗濯ネームサイズ印字", "絵表示", "洗濯ネーム用付記用語1", "洗濯ネーム用付記用語2", "洗濯ネーム用付記用語3", "洗濯ネーム用付記用語4", "洗濯ネーム用付記用語5", "洗濯ネーム用付記用語6",
                "洗濯ネーム用付記用語7", "洗濯ネーム用付記用語8", "洗濯ネーム用付記用語9", "洗濯ネーム用付記用語10", "洗濯ネーム用付記用語11", "洗濯ネーム用付記用語12", "アテンションタグ用付記用語1", "アテンションタグ用付記用語2",
                "アテンションタグ用付記用語3", "アテンションタグ用付記用語4", "アテンションタグ用付記用語5", "アテンションタグ用付記用語6", "アテンションタグ用付記用語7", "アテンションタグ用付記用語8", "アテンションタグ用付記用語9", "アテンションタグ用付記用語10",
                "アテンションタグ用付記用語11", "アテンションタグ用付記用語12", "部位1", "素材1", "混率1", "部位2", "素材2", "混率2", "部位3", "素材3", "混率3", "部位4", "素材4", "混率4", "部位5", "素材5", "混率5",
                "部位6", "素材6", "混率6", "部位7", "素材7", "混率7", "部位8", "素材8", "混率8", "部位9", "素材9", "混率9", "部位10", "素材10", "混率10", "部位11", "素材11", "混率11", "部位12",
                "素材12", "混率12", "部位13", "素材13", "混率13", "部位14", "素材14", "混率14", "部位15", "素材15", "混率15", "部位16", "素材16", "混率16", "部位17", "素材17", "混率17", "部位18",
                "素材18", "混率18", "部位19", "素材19", "混率19", "部位20", "素材20", "混率20", "NERGY用メリット下札コード1", "NERGY用メリット下札コード2", "NERGY用メリット下札コード3", "NERGY用メリット下札コード4",
                "NERGY用メリット下札コード5", "NERGY用メリット下札コード6", "QRコードの有無", "シールへの絵表示印字", "シールへの付記用語印字", "シールへの品質印字", "シールへのリサイクルマーク印字", "アテンションシールのシール種類", "製品分類",
                "製品種別", "産地（原産地）", "サスティナブルマーク印字");
    }

    /**
     * 品番情報ボディ行を出力する.
     *
     * @param printer
     *            PrintWriter
     * @param fukukitaruOrderInfoModel
     *            フクキタル発注情報モデル
     * @throws IOException
     *             IOException
     */
    private void printItemInfoBody(final CSVPrinter printer, final FukukitaruOrderInfoModel fukukitaruOrderInfoModel) throws IOException {
        // 品番情報の色、サイズのパターン情報
        for (TSkuEntity tSkuEntity : fukukitaruOrderInfoModel.getListTSkuEntity()) {
            // カラーコードに一致する、または、共通の組成情報を取得
            final List<ExtendedTCompositionLinkingEntity> listComposition = filterComposition(tSkuEntity.getColorCode(),
                    fukukitaruOrderInfoModel.getListExtendedTCompositionEntity());
            // カラーコードに一致する、または、共通の洗濯ネーム付記用語を取得
            final List<ExtendedTFWashAppendicesTermEntity> listWashAppendicesTerm = filterWashAppendicesTerm(tSkuEntity.getColorCode(),
                    fukukitaruOrderInfoModel.getListTFWashAppendicesTermEntity());
            // カラーコードに一致する、または、共通のアテンションタグ付記用語を取得
            final List<ExtendedTFAttentionAppendicesTermEntity> listAttentionAppendicesTerm = filterAttentionAppendicesTerm(tSkuEntity.getColorCode(),
                    fukukitaruOrderInfoModel.getListTFAttentionAppendicesTermEntity());
            // テープ種別を取得（テープ種類にSP1000が指定されている、 かつ、洗濯ネーム付記用語にF-502(インディゴ染)の指定がある場合、 自動的にテープ種類をSP6600(洗い加工用)に変更する）
            final String tapeName = chengedTape(fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getTapeName(), listWashAppendicesTerm);
            // 産地（原産地）
            String cooName = "";
            if (StringUtils.isNotEmpty(fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCnProductCategoryCode())
                    || StringUtils.isNotEmpty(fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCnProductTypeCode())) {
                cooName = fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCooName();
            }

            int indexComposition = 0;
            int indexWash = 0;
            int indexAttention = 0;
            printer.printRecord(fukukitaruOrderInfoModel.getLinkingOrderInfoEntity().getOrderCode(), // オーダー識別コード
                    fukukitaruOrderInfoModel.getLinkingOrderInfoEntity().getBrandName(), // ブランド名
                    fukukitaruOrderInfoModel.getLinkingOrderInfoEntity().getBrandCode(), // ブランド名記号
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getReefurPrivateBrandCode(), // REEFUR用ブランド
                    BusinessUtils.formatPartNo(fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPartNo()), // 製品品番
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getSeasonCode(), // 季別
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCategoryCode(), // カテゴリコード
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getSaturdaysPrivateNyPartNo(), // サタデーズサーフ用NY品番
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCooName(), // 原産国
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getRetailPrice(), // 上代
                    "", // アイテム名
                    "", // アイテム種類
                    tSkuEntity.getColorCode(), // カラー
                    tSkuEntity.getSize(), // サイズ
                    tapeName, // 洗濯ネームテープ種類
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getTapeWidthName(), // 洗濯ネームテープ巾
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintSize(), // 洗濯ネームサイズ印字
                    filterWashPatternCodeByColorCode(tSkuEntity.getColorCode(), fukukitaruOrderInfoModel.getListTFWashPatternEntity()), // 絵表示

                    // 洗濯ネーム用付記用語1 ～ 洗濯ネーム用付記用語12
                    getListByIndex(listWashAppendicesTerm, indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listWashAppendicesTerm, ++indexWash, ExtendedTFWashAppendicesTermEntity.class).getAppendicesTermCode(),

                    // アテンションタグ用付記用語1 ～ アテンションタグ用付記用語12
                    getListByIndex(listAttentionAppendicesTerm, indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),
                    getListByIndex(listAttentionAppendicesTerm, ++indexAttention, ExtendedTFAttentionAppendicesTermEntity.class).getAppendicesTermCode(),

                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位1
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材1
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率1
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位2
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材2
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率2
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位3
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材3
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率3
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位4
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材4
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率4
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位5
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材5
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率5
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位6
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材6
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率6
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位7
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材7
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率7
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位8
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材8
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率8
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位9
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材9
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率9
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位10
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材10
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率10
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位11
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材11
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率11
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位12
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材12
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率12
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位13
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材13
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率13
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位14
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材14
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率14
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位15
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材15
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率15
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位16
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材16
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率16
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位17
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材17
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率17
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位18
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材18
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率18
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位19
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材19
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率19
                    getListByIndex(listComposition, ++indexComposition, ExtendedTCompositionLinkingEntity.class).getParts(), // 部位20
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getComposition(), // 素材20
                    getListByIndex(listComposition, indexComposition, ExtendedTCompositionLinkingEntity.class).getPercent(), // 混率20
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode1(), // NERGY用メリット下札コード1
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode2(), // NERGY用メリット下札コード2
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode3(), // NERGY用メリット下札コード3
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode4(), // NERGY用メリット下札コード4
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode5(), // NERGY用メリット下札コード5
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getNergyBillCode6(), // NERGY用メリット下札コード6
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintQrcode(), // QRコードの有無
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintWashPattern(), // シールへの絵表示印字
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintAppendicesTerm(), // シールへの付記用語印字
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintParts(), // シールへの品質印字
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getRecycleCode(), // シールへのリサイクルマーク印字
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getSealCode(), // アテンションシールのシール種類
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCnProductCategoryCode(), // 製品分類
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getCnProductTypeCode(), // 製品種別
                    cooName, // 産地（原産地）
                    fukukitaruOrderInfoModel.getExtendedTFItemLinkingEntity().getPrintSustainableMark() // サスティナブルマーク印字
            );
        }
    }

    /**
     * テープ種類にSP1000が指定されている、 かつ、洗濯ネーム付記用語にF-502(インディゴ染)の指定がある場合、 自動的にテープ種類をSP6600(洗い加工用)に変更する.
     *
     * @param tapeName
     *            テープ種類
     * @param listTFWashAppendicesTermEntity
     *            洗濯ネーム付記用語リスト
     * @return テープ種類
     */
    private String chengedTape(final String tapeName, final List<ExtendedTFWashAppendicesTermEntity> listTFWashAppendicesTermEntity) {
        if (StringUtils.isEmpty(tapeName)) {
            // テープ種類が未指定の場合は、そのまま返す
            return tapeName;
        }

        // テープ種類に「SP1000」が指定されているか判断する
        if (tapeName.equals("SP1000")) {
            // 洗濯ネーム付記用語にF-502(インディゴ染)の指定があるか検証する
            if (listTFWashAppendicesTermEntity.stream().filter(entity -> {
                if (StringUtils.isEmpty(entity.getAppendicesTermCode())) {
                    return false;
                }
                if (entity.getAppendicesTermCode().equals("F-502")) {
                    return true;
                }
                return false;
            }).findFirst().isPresent()) {
                // テープ種類をSP6600(洗い加工用)を返す
                return "SP6600";
            }
        }
        return tapeName;
    }

    /**
     * インデックスを指定してリストの要素を取得する. インデックスがlistサイズより大きい場合は、空のオブジェクトを返す
     *
     * @param <T>
     *            型
     * @param list
     *            組成情報リスト
     * @param index
     *            カラム番号
     * @param clazz
     *            クラス
     * @return リストの要素
     */
    private <T> T getListByIndex(final List<T> list, final int index, final Class<T> clazz) {

        if (list.size() > index) {
            return list.get(index);
        }

        // 空のオブジェクト
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }

    }

    /**
     * ファイル情報テーブルに登録する.
     *
     * @param id
     *            id
     * @param fileName
     *            ファイル名
     * @param s3Key
     *            S3キー
     * @param userId
     *            ユーザーID
     * @return 更新後ファイル情報
     */
    private TFLinkingFileEntity registTFLinkingFileEntity(final BigInteger id, final String fileName, final String s3Key, final BigInteger userId) {
        final TFLinkingFileEntity fileEntity = new TFLinkingFileEntity();
        fileEntity.setFOrderId(id);
        fileEntity.setContentType(CONTENT_TYPE);
        fileEntity.setFileName(fileName);
        fileEntity.setS3Key(s3Key);
        fileEntity.setS3Prefix(propertyComponent.getBatchProperty().getMaterialOrderLinking().getS3Prefix());
        fileEntity.setCreatedUserId(userId);
        fileEntity.setCreatedAt(new Date());
        fileEntity.setUpdatedUserId(userId);
        fileEntity.setUpdatedAt(new Date());
        return tFLinkingFileRepository.save(fileEntity);
    }

    /**
     * カラーコードをキーに洗濯マーク情報を取得する. カラーコードに該当するデータが存在しない場合、カラーコード共通(00)の洗濯マーク情報を返す カラーコード共通(00)の洗濯マーク情報が存在しなあい場合、NULLを返す
     *
     * @param colorCode
     *            カラーコード
     * @param list
     *            洗濯マークリスト
     * @return 洗濯マークコード
     */
    private String filterWashPatternCodeByColorCode(final String colorCode, final List<ExtendedTFWashPatternEntity> list) {
        // カラーコードに一致する情報を取得する
        final List<ExtendedTFWashPatternEntity> listColorFilter = list.stream().filter(entity -> StringUtils.equals(colorCode, entity.getColorCode()))
                .collect(Collectors.toList());
        if (!listColorFilter.isEmpty()) {
            return listColorFilter.get(0).getWashPatternCode();
        }

        // カラーコードに一致する情報が存在しない場合、カラーコード共通(00)の情報を取得する
        final List<ExtendedTFWashPatternEntity> listColorFilter00 = list.stream().filter(entity -> StringUtils.equals(COMMON_COLOR_CODE, entity.getColorCode()))
                .collect(Collectors.toList());

        if (!listColorFilter00.isEmpty()) {
            return listColorFilter00.get(0).getWashPatternCode();
        }

        return null;

    }

    /**
     * カラーコードをキーに洗濯ネーム付記用語を取得する. カラーコードに該当するデータが存在しない場合、カラーコード共通(00)の洗濯ネーム付記用語を返す
     *
     * @param colorCode
     *            カラーコード
     * @param list
     *            洗濯ネーム付記用語リスト
     * @return 洗濯ネーム付記用語
     */
    private List<ExtendedTFWashAppendicesTermEntity> filterWashAppendicesTerm(final String colorCode, final List<ExtendedTFWashAppendicesTermEntity> list) {
        // カラーコードに一致する情報を取得する
        final List<ExtendedTFWashAppendicesTermEntity> listColorFilter = list.stream().filter(entity -> StringUtils.equals(colorCode, entity.getColorCode()))
                .collect(Collectors.toList());

        if (listColorFilter.isEmpty()) {
            // カラーコードに一致する情報が存在しない場合、カラーコード共通(00)の情報を取得する
            return list.stream().filter(entity -> StringUtils.equals(COMMON_COLOR_CODE, entity.getColorCode())).collect(Collectors.toList());

        }
        return listColorFilter;
    }

    /**
     * カラーコードをキーにアテンションタグ付記用語を取得する. カラーコードに該当するデータが存在しない場合、カラーコード共通(00)のアテンションタグ付記用語を返す
     *
     * @param colorCode
     *            カラーコード
     * @param list
     *            アテンションタグ付記用語リスト
     * @return アテンションタグ付記用語
     */
    private List<ExtendedTFAttentionAppendicesTermEntity> filterAttentionAppendicesTerm(final String colorCode,
            final List<ExtendedTFAttentionAppendicesTermEntity> list) {
        // カラーコードに一致する情報を取得する
        final List<ExtendedTFAttentionAppendicesTermEntity> listColorFilter = list.stream()
                .filter(entity -> StringUtils.equals(colorCode, entity.getColorCode())).collect(Collectors.toList());

        if (listColorFilter.isEmpty()) {
            // カラーコードに一致する情報が存在しない場合、カラーコード共通(00)の情報を取得する
            return list.stream().filter(entity -> StringUtils.equals(COMMON_COLOR_CODE, entity.getColorCode())).collect(Collectors.toList());

        }
        return listColorFilter;
    }

    /**
     * カラーコードをキーに組成情報を取得する.
     *
     * @param colorCode
     *            カラーコード
     * @param list
     *            組成情報リスト
     * @return 組成情報
     */
    private List<ExtendedTCompositionLinkingEntity> filterComposition(final String colorCode, final List<ExtendedTCompositionLinkingEntity> list) {
        // カラーコードに一致する情報を取得する
        final List<ExtendedTCompositionLinkingEntity> listColorFilter = list.stream().filter(entity -> StringUtils.equals(colorCode, entity.getColorCode()))
                .collect(Collectors.toList());

        return listColorFilter;
    }

    /**
     * カラーコードをキーに資材情報を取得する.
     * ※資材数量が0以下のものは取得しない
     *
     * @param colorCode
     *            カラーコード
     * @param list
     *            資材情報リスト
     * @return 資材情報
     */
    private List<ExtendedTFOrderSkuEntity> filterMaterial(final String colorCode, final List<ExtendedTFOrderSkuEntity> list) {
        // カラーコードに一致 かつ 資材数量>0 の資材情報を取得する
        final List<ExtendedTFOrderSkuEntity> listColorFilter = list.stream()
                .filter(entity -> StringUtils.equals(colorCode, entity.getColorCode()) && entity.getOrderLot().intValue() > 0)
                .collect(Collectors.toList());
        return listColorFilter;
    }

    /**
     * 重複しないカラーコードを取得.
     * ※共通(00)は含まない
     *
     * @param skuList
     *            品番SKUリスト
     * @return カラーコードリスト
     */
    private List<String> getUniqueColorList(final List<TSkuEntity> skuList) {
        return skuList.stream()
                .filter(colorCodeSku -> !StringUtils.equals(colorCodeSku.getColorCode(), COMMON_COLOR_CODE))
                .map(sku -> sku.getColorCode())
                .distinct()
                .collect(Collectors.toList());
    }
}
