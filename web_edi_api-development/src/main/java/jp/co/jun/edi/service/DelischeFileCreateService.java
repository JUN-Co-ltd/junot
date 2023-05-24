package jp.co.jun.edi.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DelischeComponent;
import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.entity.TDelischeFileInfoEntity;
import jp.co.jun.edi.entity.TFileEntity;
import jp.co.jun.edi.entity.VDelischeCsvEntity;
import jp.co.jun.edi.entity.key.VDelischeCsvKey;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.model.DelischeFileInfoModel;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TDelischeFileInfoRepository;
import jp.co.jun.edi.repository.TFileRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.DelischeFileStatusType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.ObjectMapperUtil;
import jp.co.jun.edi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
/**
 * デリスケファイルを作成するサービス.
 */
@Service
@Slf4j
public class DelischeFileCreateService extends GenericCreateService<CreateServiceParameter<DelischeFileInfoModel>,
CreateServiceResponse<DelischeFileInfoModel>> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    private SessionFactory sessionFactory;

    @Autowired
    private DelischeComponent delischeComponent;

    @Autowired
    private S3Component s3Component;

    @Autowired
    private TFileRepository tFileRepository;

    @Autowired
    private TDelischeFileInfoRepository tDelischeFileInfoRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Value("${properties.jp.co.jun.edi.thread-delische.tmp-directory}")
    private String tmpDirectory;

    @Value("${properties.jp.co.jun.edi.thread-delische.tmp-file-prefix}")
    private String tmpFilePrefix;

    @Value("${properties.jp.co.jun.edi.thread-delische.tmp-file-suffix}")
    private String tmpFileSuffix;

    @Value("${cloud.aws.s3.delische-csv-prefix}")
    private String s3Prefix;

    @Value("${cloud.aws.s3.max-delische-csv-cnt}")
    private int maxDelischeCsvCnt;

    private static final String CONTENT_TYPE = "text/csv";

    private static final int FETCH_SIZE = 1000;

    private static final int DEFAULT_COLMUN_NUM = 24;

    /**
     * @param factory EntityManagerFactory
     */
    @Autowired
    public DelischeFileCreateService(final EntityManagerFactory factory) {
        this.sessionFactory = factory.unwrap(SessionFactory.class);

        if (this.sessionFactory == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
    }

    @Override
    protected CreateServiceResponse<DelischeFileInfoModel> execute(final CreateServiceParameter<DelischeFileInfoModel> serviceParameter) {

        final CustomLoginUser loginUser = serviceParameter.getLoginUser();
        final BigInteger delischeFileInfoId = serviceParameter.getItem().getId();
        final TDelischeFileInfoEntity tDelischeFileInfoEntity = tDelischeFileInfoRepository.findById(delischeFileInfoId).orElse(null);
        final DelischeOrderSearchConditionModel delischeOrderSearchCondition =
                ObjectMapperUtil.readValue(tDelischeFileInfoEntity.getSearchConditions(), DelischeOrderSearchConditionModel.class);
        final List<String> brandCodeListFromDivision =
                mCodmstRepository.findBrandCodesByDivisionCode(MCodmstTblIdType.BRAND.getValue(), delischeOrderSearchCondition.getDivisionCode());
        delischeOrderSearchCondition.setBrandCodeListFromDivision(brandCodeListFromDivision);

        File file = null;
        final BigInteger userId = loginUser.getUserId();

        try {
            final Path tmpPath = Files.createTempFile(Paths.get(tmpDirectory), tmpFilePrefix, tmpFileSuffix);
            file = tmpPath.toFile();

            try (Session session = sessionFactory.openSession();
                    Writer wt = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
                    CSVPrinter printer = new CSVPrinter(wt, CSVFormat.EXCEL);) {

                final StringBuilder sql = new StringBuilder();
                delischeComponent.generateDelischeCsvSql(delischeOrderSearchCondition, sql);

                // デリスケCSVレコード取得
                final NativeQuery<VDelischeCsvEntity> searchQuery = session.createNativeQuery(sql.toString(), VDelischeCsvEntity.class)
                        .setReadOnly(true)
                        .setFetchSize(FETCH_SIZE)
                        .setCacheable(false);
                delischeComponent.setQueryParameters(sql, delischeOrderSearchCondition, searchQuery);

                try (ScrollableResults results = searchQuery.scroll(ScrollMode.FORWARD_ONLY)) {
                    // ファイル出力
                    fileWrite(printer, results);
                }
            }

            // S3アップロード
            final String fileName = tmpFilePrefix + "_" + ZonedDateTime.now().format(DATE_TIME_FORMATTER) + tmpFileSuffix;
            final String s3Key = s3Component.upload(file, s3Prefix, CONTENT_TYPE);

            // ファイルテーブル登録
            final TFileEntity tFileEntity = registTFile(fileName, s3Key, userId);

            // デリスケファイル情報テーブル更新
            tDelischeFileInfoRepository.updateFileNoIdAndStatus(tFileEntity.getId(),
                    DelischeFileStatusType.COMPLETE_CREATE.getValue(), userId, delischeFileInfoId);

        } catch (BusinessException e) {
            log.warn("CSV max record count over.", e);
            // デリスケファイル情報テーブル更新
            tDelischeFileInfoRepository.updateFileNoIdAndStatus(null,
                    DelischeFileStatusType.CNT_ERROR.getValue(), userId, delischeFileInfoId);
        } catch (IOException e) {
            log.error("IOException occurred.", e);
            // デリスケファイル情報テーブル更新
            tDelischeFileInfoRepository.updateFileNoIdAndStatus(null,
                    DelischeFileStatusType.OTHER_ERROR.getValue(), userId, delischeFileInfoId);
        } finally {
            // ファイルを削除
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    log.error("delische file delete error.");
                }
            }
        }

        return CreateServiceResponse.<DelischeFileInfoModel>builder().item(new DelischeFileInfoModel()).build();
    }

    /**
     * CSV出力する.
     * @param printer CSVPrinter
     * @param results 取得レコード
     * @throws IOException IOException
     * @throws BusinessException BusinessException
     */
    private void fileWrite(final CSVPrinter printer, final ScrollableResults results) throws IOException, BusinessException {
        printer.print('\ufeff');    // BOM付与
        printHeaderInfo(printer);   // ヘッダー行出力

        BigInteger previousOrderId = null;
        BigInteger previousDeliveryId = null;
        Integer previousDeliveryCount = null;
        Date previousDeliveryAt = null;

        int csvRecordCnt = 0;
        while (results.next()) {
            VDelischeCsvEntity vDelischeCsv = (VDelischeCsvEntity) results.get(0);
            final VDelischeCsvKey vDelischeCsvKey = vDelischeCsv.getVDelischeCsvKey();
            final BigInteger currentOrderId = vDelischeCsvKey.getOrderId();
            final BigInteger currentDeliveryId = vDelischeCsvKey.getDeliveryId();
            final Integer currentDeliveryCount = vDelischeCsvKey.getDeliveryCount();
            final Date currentDeliveryAt = vDelischeCsvKey.getDeliveryAt();

            if (!Objects.equals(currentOrderId, previousOrderId)) {
                previousOrderId = currentOrderId;
                csvRecordCnt = addCnt(csvRecordCnt);
                // 発注レコード出力
                printDelischeOrderRecord(printer, vDelischeCsv);
            }

            if (!Objects.equals(currentOrderId, previousOrderId)
                    || !Objects.equals(currentDeliveryId, previousDeliveryId)
                    || !Objects.equals(currentDeliveryCount, previousDeliveryCount)
                    || !Objects.equals(currentDeliveryAt, previousDeliveryAt)) {
                previousDeliveryId = currentDeliveryId;
                previousDeliveryCount = currentDeliveryCount;
                previousDeliveryAt = currentDeliveryAt;
                if (!currentDeliveryId.equals(BigInteger.ZERO)) {
                    csvRecordCnt = addCnt(csvRecordCnt);
                    // 納品依頼レコード出力
                    printDelischeDeliveryRequestRecord(printer, vDelischeCsv);
                }
            }

            if (!org.apache.commons.lang3.StringUtils.isEmpty(vDelischeCsvKey.getColorCode())
                    && !org.apache.commons.lang3.StringUtils.isEmpty(vDelischeCsvKey.getSize())) {
                csvRecordCnt = addCnt(csvRecordCnt);
                // 納品SKUレコード出力
                printDelischeDeliverySkuRecord(printer, vDelischeCsv);
            }

        }
    }

    /**
     * レコード件数を加算する.
     * 最大件数を超えた倍はBusinessException
     * @param currentRecordCnt 現在の件数
     * @return 加算後件数
     * @throws BusinessException BusinessException
     */
    private int addCnt(final int currentRecordCnt) throws BusinessException {
        final int addCnt = currentRecordCnt + 1;
        if (addCnt > maxDelischeCsvCnt) {
            throw new BusinessException();
        }
        return addCnt;
    }

    /**
     * ファイル情報テーブルに登録する.
     * @param fileName ファイル名
     * @param s3Key S3キー
     * @param userId ユーザーID
     * @return 更新後ファイル情報
     */
    private TFileEntity registTFile(final String fileName, final String s3Key, final BigInteger userId) {
        final TFileEntity fileEntity = new TFileEntity();
        fileEntity.setContentType(CONTENT_TYPE);
        fileEntity.setFileName(fileName);
        fileEntity.setS3Key(s3Key);
        fileEntity.setS3Prefix(s3Prefix);
        fileEntity.setCreatedUserId(userId);
        fileEntity.setCreatedAt(new Date());
        fileEntity.setUpdatedUserId(userId);
        fileEntity.setUpdatedAt(new Date());
        return tFileRepository.save(fileEntity);
    }

    /**
     * ヘッダー行を出力する.
     * @param printer PrintWriter
     * @throws IOException IOException
     */
    private void printHeaderInfo(final CSVPrinter printer) throws IOException {
        printer.printRecord(
                "レコード区分",
                "月度",
                "納品週",
                "ブランド",
                "アイテム",
                "品番",
                "品名",
                "上代合計",
                "原価合計",
                "カラー",
                "サイズ",
                "シーズン",
                "メーカー",
                "生産発注日",
                "生産工程",
                "生産納期",
                "納品日",
                "発注数",
                "納品依頼数",
                "仕入実数",
                "発注残",
                "売上数",
                "在庫数",
                "上代単価",
                "原価単価",
                "原価率");
    }

    /**
     * デリスケ発注レコードを出力する.
     * @param printer PrintWriter
     * @param vDelischeCsv デリスケCSVレコード
     * @throws IOException IOException
     */
    private void printDelischeOrderRecord(final CSVPrinter printer, final VDelischeCsvEntity vDelischeCsv) throws IOException {
        final Integer orderRemainingLot = delischeComponent.calculateOrderRemainingLot(vDelischeCsv.getQuantity(), vDelischeCsv.getArrivalLotOrderSum());
        final Integer netSalesQuantity = vDelischeCsv.getPosSalesQuantity();
        final Integer stockLot = delischeComponent.calculateStockLot(vDelischeCsv.getArrivalLotOrderSum(), netSalesQuantity);
        final BigDecimal calculateRetailPrice = delischeComponent.calculateRetailPrice(vDelischeCsv.getQuantity(), vDelischeCsv.getRetailPrice());
        final BigDecimal calculateProductCost = delischeComponent.calculateProductCost(vDelischeCsv.getQuantity(), vDelischeCsv.getProductCost());
        final double costRate = delischeComponent.calculateCostRate(vDelischeCsv.getRetailPrice(), vDelischeCsv.getProductCost());
        final List<Object> values = new ArrayList<>(DEFAULT_COLMUN_NUM);
        values.add("発注");    // レコード区分
        values.add(vDelischeCsv.getProductDeliveryAtMonthly());  // 月度
        values.add(BusinessUtils.formatMdWeek(vDelischeCsv.getProductDeliveryAt())); // 納品週
        values.add(vDelischeCsv.getBrandCode()); // ブランド
        values.add(vDelischeCsv.getItemCode());  // アイテム
        values.add(vDelischeCsv.getPartNo());    // 品番
        values.add(vDelischeCsv.getProductName());   // 品名
        values.add(calculateRetailPrice);    // 上代合計
        values.add(calculateProductCost);    // 原価合計
        values.add("");  // カラー
        values.add("");  // サイズ
        values.add(vDelischeCsv.getSeason());    // シーズン
        values.add(vDelischeCsv.getMdfMakerName());  // メーカー
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductOrderAt()));   // 発注日
        values.add(BusinessUtils.formatDelischeProductionStatus(vDelischeCsv.getProductionStatus()));    // 生産工程
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductDeliveryAt()));    // 発注納期
        values.add(BusinessUtils.formatLateDeliveryAtCnt(vDelischeCsv.getLateDeliveryAtCnt()));  // 納品日
        values.add(vDelischeCsv.getQuantity());  // 発注数
        values.add(StringUtils.defaultString(vDelischeCsv.getDeliveryLotOrderSum()));    // 納品依頼数
        values.add(StringUtils.defaultString(vDelischeCsv.getArrivalLotOrderSum())); // 仕入実数
        values.add(StringUtils.defaultString(orderRemainingLot));    // 発注残
        values.add(StringUtils.defaultString(netSalesQuantity));    // 売上数
        values.add(StringUtils.defaultString(stockLot));    // 在庫数
        values.add(vDelischeCsv.getRetailPrice());   // 上代単価
        values.add(vDelischeCsv.getProductCost());   // 原価単価
        values.add(costRate + "％");  // 原価率
        printer.printRecord(values);
    }

    /**
     * デリスケ納品依頼レコードを出力する.
     * @param printer PrintWriter
     * @param vDelischeCsv デリスケCSVレコード
     * @throws IOException IOException
     */
    private void printDelischeDeliveryRequestRecord(final CSVPrinter printer, final VDelischeCsvEntity vDelischeCsv) throws IOException {
        final BigDecimal calculateRetailPrice = delischeComponent.calculateRetailPrice(vDelischeCsv.getDeliveryLotSum(), vDelischeCsv.getRetailPrice());
        final BigDecimal calculateProductCost = delischeComponent.calculateProductCost(vDelischeCsv.getDeliveryLotSum(), vDelischeCsv.getProductCost());
        final VDelischeCsvKey key = vDelischeCsv.getVDelischeCsvKey();
        final String deliveryCount = BusinessUtils.formatDeliveryCount(key);
        final List<Object> values = new ArrayList<>(DEFAULT_COLMUN_NUM);
        values.add("納品依頼");  // レコード区分
        values.add(vDelischeCsv.getDeliveryAtMonthly()); // 月度
        values.add(BusinessUtils.formatMdWeek(key.getDeliveryAt())); // 納品週
        values.add(vDelischeCsv.getBrandCode()); // ブランド
        values.add(vDelischeCsv.getItemCode());  // アイテム
        values.add(vDelischeCsv.getPartNo());    // 品番
        values.add(vDelischeCsv.getProductName());   // 品名
        values.add(calculateRetailPrice);    // 上代合計
        values.add(calculateProductCost);    // 下代合計
        values.add("");  // カラー
        values.add("");  // サイズ
        values.add(vDelischeCsv.getSeason());    // シーズン
        values.add(vDelischeCsv.getMdfMakerName());  // メーカー
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductOrderAt()));   // 発注日
        values.add("");  // 生産工程
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductDeliveryAt()));    // 発注納期
        values.add(DateUtils.formatYMD(key.getDeliveryAt()) + "(" + deliveryCount + ")");    // 納品日,納品依頼回数
        values.add(vDelischeCsv.getProductOrderLotSum());    // 発注数
        values.add(vDelischeCsv.getDeliveryLotSum());    // 納品依頼数
        values.add(vDelischeCsv.getArrivalLotSum()); // 仕入実数
        values.add("");  // 発注残
        values.add("");  // 売上数
        values.add("");  // 在庫数
        values.add("");  // 上代単価
        values.add("");  // 下代単価
        values.add("");  // 原価率
        printer.printRecord(values);
    }

    /**
     * デリスケ納品SKUレコードを出力する.
     * @param printer PrintWriter
     * @param vDelischeCsv デリスケCSVレコード
     * @throws IOException IOException
     */
    private void printDelischeDeliverySkuRecord(final CSVPrinter printer, final VDelischeCsvEntity vDelischeCsv) throws IOException {
        final BigDecimal calculateRetailPrice = delischeComponent.calculateRetailPrice(vDelischeCsv.getDeliveryLot(), vDelischeCsv.getRetailPrice());
        final BigDecimal calculateProductCost = delischeComponent.calculateProductCost(vDelischeCsv.getDeliveryLot(), vDelischeCsv.getProductCost());
        final VDelischeCsvKey key = vDelischeCsv.getVDelischeCsvKey();
        final List<Object> values = new ArrayList<>(DEFAULT_COLMUN_NUM);
        values.add("納品SKU"); // レコード区分
        values.add(vDelischeCsv.getDeliveryAtMonthly()); // 月度
        values.add(BusinessUtils.formatMdWeek(key.getDeliveryAt())); // 納品週
        values.add(vDelischeCsv.getBrandCode()); // ブランド
        values.add(vDelischeCsv.getItemCode());  // アイテム
        values.add(vDelischeCsv.getPartNo());    // 品番
        values.add(vDelischeCsv.getProductName());   // 品名
        values.add(calculateRetailPrice);    // 上代合計
        values.add(calculateProductCost);    // 下代合計
        values.add(key.getColorCode());  // カラー
        values.add(key.getSize());   // サイズ
        values.add(vDelischeCsv.getSeason());    // シーズン
        values.add(vDelischeCsv.getMdfMakerName());  // メーカー
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductOrderAt()));   // 発注日
        values.add("");  // 生産工程
        values.add(DateUtils.formatYMD(vDelischeCsv.getProductDeliveryAt()));    // 発注納期
        values.add(DateUtils.formatYMD(key.getDeliveryAt()));    // 納品日
        values.add(vDelischeCsv.getProductOrderLot());   // 発注数
        values.add(vDelischeCsv.getDeliveryLot());   // 納品依頼数
        values.add(vDelischeCsv.getArrivalLot());    // 仕入実数
        values.add("");  // 発注残
        values.add("");  // 売上数
        values.add("");  // 在庫数
        values.add("");  // 上代単価
        values.add("");  // 下代単価
        values.add("");  // 原価率
        printer.printRecord(values);
    }
}
