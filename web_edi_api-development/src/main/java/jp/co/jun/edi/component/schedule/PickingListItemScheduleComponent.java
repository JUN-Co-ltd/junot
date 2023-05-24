package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.DirectDeliveryItemCreatePDFComponent;
import jp.co.jun.edi.component.PickingListItemCreateXmlComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 直送（伝票）PDF生成スケジュールのコンポーネント.
 *
 * ・ピッキングリスト
 */
@Slf4j
@Component
public class PickingListItemScheduleComponent extends DirectDeliveryItemScheduleComponent {
    private static final String VOUCHER_NAME = "picking_list";
    private static final String TEMP_DIRECTORY = "pickingListItem";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PickingListItemCreateXmlComponent createXmlComponent;

    @Autowired
    private DirectDeliveryItemCreatePDFComponent createPdfComponent;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TDeliveryDetailRepository detailRepository;

    /**
     * 伝票作成処理の実行.
     *
     * @param entity 伝票管理情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final TDeliveryVoucherFileInfoEntity entity) {
        log.info(LogStringUtil.of("execute")
                .message("Start processing of PickingListItemSchedule.")
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
                    propertyComponent.getBatchProperty().getPickingListPathXsl(),
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

        // ファイル名：品番-発注番号CHAR(6)-回数-納品番号CHAR(6)-PK.pdf
        final String fileName = String.format("%s-%s-%s-%s-PK.pdf",
                odrEntity.getPartNo(),
                odrEntity.getOrderNumber(),
                deliveryCount,
                deliveryEntity.getDeliveryNumber()
                );
        return fileName;
    }
}
