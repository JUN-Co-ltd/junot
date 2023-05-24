package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.DirectDeliveryItemCreatePDFComponent;
import jp.co.jun.edi.component.DirectDeliveryItemCreateXmlComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryVoucherFileInfoRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 直送（伝票）PDF生成スケジュールのコンポーネント.
 *
 * ・出荷配分伝票
 */
@Slf4j
@Component
public class DirectDeliveryItemScheduleComponent {
    private static final String VOUCHER_NAME = "direct_delivery";
    private static final String TEMP_DIRECTORY = "directDeliveryItem";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private TDeliveryVoucherFileInfoRepository repository;

    @Autowired
    private DirectDeliveryItemCreateXmlComponent createXmlComponent;

    @Autowired
    private DirectDeliveryItemCreatePDFComponent createPdfComponent;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TDeliveryDetailRepository detailRepository;

    /**
     * 伝票作成処理の実行.
     *
     * @param entity 返品伝票管理情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final TDeliveryVoucherFileInfoEntity entity) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of DirectDeliveryItemSchedule.")
                .value("id", entity.getId())
                .value("voucher_category", entity.getVoucherCategory())
                .value("delivery_id", entity.getDeliveryId())
                .value("delivery_count", entity.getDeliveryCount())
                .value("order_id", entity.getOrderId())
                .build());

        final BigInteger userId = scheduleBusinessComponent.getUserId();

        try {
            // プロパティ情報取得
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(entity.getId(), TEMP_DIRECTORY);

            // XMLファイルパス
            final Path xmlPath =
                    generatedFilePath(temporayFolder,
                            VOUCHER_NAME,
                            entity.getVoucherCategory().getValue(),
                            entity.getOrderId(),
                            ".xml");

            // PDFファイルパス
            final Path pdfPath =
                    generatedFilePath(temporayFolder,
                            VOUCHER_NAME,
                            entity.getVoucherCategory().getValue(),
                            entity.getOrderId(),
                            ".pdf");

            // XSLファイルパス
            final Path xslPath = getXslPath(
                    propertyComponent.getBatchProperty().getDirectDeliveryPathXsl(),
                    VOUCHER_NAME);

            // PDFファイル名(一時ファイル→正式なファイル名に変更)
            final String fileName = generatedPDFFileName(
                    entity.getDeliveryId(),
                    entity.getDeliveryCount(),
                    entity.getOrderId());

            // XML作成
            createXmlComponent.createXml(
                    entity.getDeliveryId(),
                    entity.getDeliveryCount(),
                    entity.getOrderId(),
                    xmlPath);

            // PDF作成
            final BigInteger fileId = createPdfComponent.createPdf(xslPath, xmlPath, pdfPath, userId, fileName);

            // 一時フォルダのXMLファイル、PDFファイルを削除
            deleteFile(temporayFolder, xmlPath, pdfPath);

            log.info(LogStringUtil.of("execute")
                    .message("End processing of DirectDeliveryItemSchedule.")
                    .value("id", entity.getId())
                    .value("voucher_category", entity.getVoucherCategory())
                    .value("delivery_id", entity.getDeliveryId())
                    .value("delivery_count", entity.getDeliveryCount())
                    .value("order_id", entity.getOrderId())
                    .build());

            // ステータスを 処理完了 に更新
            updateStatus(FileInfoStatusType.FILE_COMPLETED, fileId, entity, userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatus(FileInfoStatusType.FILE_ERROR, null, entity, userId);
        }
    }

    /**
     * PDFファイル名 生成.
     *
     * @param deliveryId 納品ID
     * @param deliveryCount 納品依頼回数
     * @param orderId 発注ID
     * @return PDF名
     */
    private String generatedPDFFileName(
            final BigInteger deliveryId,
            final Integer deliveryCount,
            final BigInteger orderId) {
        final TOrderEntity odrEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002,
                                LogStringUtil.of("generatedPDFFileName")
                                .message("t_order not found.")
                                .value("order_id", orderId)
                                .build()
                                )));

        final PageRequest pageRequest = PageRequest.of(0, 1);

        final TDeliveryDetailEntity deliveryEntity =
                detailRepository.findByDeliveryId(deliveryId, pageRequest).getContent().get(0);
        if (deliveryEntity == null) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002,
                            LogStringUtil.of("generatedPDFFileName")
                            .message("t_delivery_detail not found.")
                            .value("delivery_id", deliveryId)
                            .value("delivery_count", deliveryCount)
                            .build()
                            ));
        }

        // ファイル名：発注番号CHAR(6)-回数-納品番号CHAR(6).pdf
        final String fileName = String.format("%s-%s-%s.pdf",
                odrEntity.getOrderNumber(),
                deliveryCount,
                deliveryEntity.getDeliveryNumber()
                );
        return fileName;
    }

    /**
     * 一時ディレクトリを取得する.
     *
     * ディレクトリ構成：{一時ディレクトリ}/{伝票名}/{ユーザID}/
     * @param userId ユーザID
     * @param voucherDirectoryName 伝票名
     * @return 発注別の一時ディレクトリ
     * @throws Exception 例外
     */
    protected String getTemporaryPath(final BigInteger userId, final String voucherDirectoryName
            ) throws Exception {
        String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
        if (!tmpDirectory.endsWith("/")) {
            // 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
            tmpDirectory = tmpDirectory.concat("/");
        }
        tmpDirectory = tmpDirectory.concat(voucherDirectoryName + "/").concat(userId.toString()).concat("/");
        Files.createDirectories(Paths.get(tmpDirectory));

        return tmpDirectory;
    }

    /**
     * ファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param voucherName 伝票名
     * @param voucherCategory 伝票タイプ
     * @param orderId 発注ID
     * @param extension 拡張子
     * @return ファイルパス
     */
    protected Path generatedFilePath(
            final String path,
            final String voucherName,
            final int voucherCategory,
            final BigInteger orderId,
            final String extension
            ) {
        // XMLファイル {伝票名}{伝票番号}{発注ID}_{yyyyMMddHHmmssSSS}.xml (or .pdf)
        final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssSSS");
        final String filePath = voucherName + voucherCategory + orderId.toString() + "_" + nowDate + extension;

        return Paths.get(path + filePath);
    }

    /**
     * XSLファイルパスを取得.
     *
     * @param strXslPath XSLファイル指定プロパティ情報
     * @param voucherName 伝票名
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    protected Path getXslPath(final String strXslPath, final String voucherName
            ) throws Exception {
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getXslPath")
                    .message("not found path.")
                    .value(VOUCHER_NAME + ".path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }

    /**
     * 処理対象の伝票管理情報のステータスを更新.
     *
     * @param status SendMailStatusType
     * @param fileId ファイル情報ID
     * @param entity 納品出荷ファイル情報
     * @param userId ユーザID
     */
    protected void updateStatus(
            final FileInfoStatusType status,
            final BigInteger fileId,
            final TDeliveryVoucherFileInfoEntity entity,
            final BigInteger userId) {
        entity.setStatus(status);
        entity.setFileNoId(fileId);
        entity.setUpdatedUserId(userId);
        repository.save(entity);
    }

    /**
     * ファイル削除.
     *
     * @param temporayFolder 一時フォルダ
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     */
    protected void deleteFile(final String temporayFolder, final Path xmlPath, final Path pdfPath) {
        try {
            // 一時フォルダのXMLファイルを削除
            Files.delete(xmlPath);
            // 一時フォルダのPDFファイルを削除
            Files.delete(pdfPath);
            // 一時ファイルと発注一時ディレクトリを削除
            Files.delete(Paths.get(temporayFolder));
        } catch (Exception e) {
            // ファイル削除失敗の場合は、ワーニングログを表示するが、処理は正常処理とする
            log.warn(e.getMessage());
        }
    }
}
