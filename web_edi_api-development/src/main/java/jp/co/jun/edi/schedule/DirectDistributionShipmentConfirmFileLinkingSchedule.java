package jp.co.jun.edi.schedule;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.DirectDistributionShipmentConfirmLinkingCreateCsvFileComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.DirectDistributionShipmentConfirmFileLinkingScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 直送配分出荷確定ファイル作成スケジュール.
 * ・ CSVファイルをS3へアップロード
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
+ ".schedule.direct-distribution-shipment-confirm-file-linking-schedule.enabled", matchIfMissing = true)
public class DirectDistributionShipmentConfirmFileLinkingSchedule {

    private static final String PROPERTY_NAME_PREFIX =
            PropertyName.ROOT + ".schedule.direct-distribution-shipment-confirm-file-linking-schedule";
    private static final String PROPERTY_NAME_CRON =
            "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private DirectDistributionShipmentConfirmLinkingCreateCsvFileComponent directDistributionShipmentConfirmLinkingCreateCsvFileComponent;

    @Autowired
    private DirectDistributionShipmentConfirmFileLinkingScheduleComponent directDistributionShipmentConfirmFileLinkingScheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    /**
     * 直送配分出荷確定ファイル連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.DIRECT_DISTRIBUTION_SHIPMENT_CONFIRM,
                        WmsLinkingStatusType.FILE_NOT_CREATE,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));

        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // WMS連携ステータスを更新(ファイル作成中)
            directDistributionShipmentConfirmFileLinkingScheduleComponent.updateWmsLinkingStatusFileCreating(wmsLinkingFiles, userId);
            // CSVファイル作成
            execute(wmsLinkingFiles, userId);
        }
    }

    /**
     * 直送配分出荷確定ファイル作成の実行.
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void execute(final List<TWmsLinkingFileEntity> wmsLinkingFiles,
            final BigInteger userId) {
        try {
            // 倉庫連携ファイル情報毎にスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {

                log.info(LogStringUtil.of("executeByWmsLinkingFile")
                        .message("Start processing of DirectDistributionShipmentConfirmFileLinkingSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("id", wmsLinkingFileEntity.getId())
                        .value("manage_number", wmsLinkingFileEntity.getManageNumber())
                        .build());

                final BigInteger wmsLinkingFileId = wmsLinkingFileEntity.getId();
                final TDeliveryEntity deliveryRecord  = tDeliveryRepository.findByWmsLinkingFileId(
                        wmsLinkingFileId).orElse(new TDeliveryEntity());

                final int purchaseFileCreatCount = tWmsLinkingFileRepository.countByPurchaseFileCreating(
                        deliveryRecord.getOrderId(), deliveryRecord.getDeliveryCount());

                // 直送仕入確定バッチが起動済みか判定
                if (purchaseFileCreatCount != 1) {
                    // ログ出力
                    log.info(LogStringUtil.of("countByPurchaseFileCreating")
                                            .message("File not created for purchase.")
                                            .value("id", wmsLinkingFileEntity.getId())
                                            .value("manage_number", wmsLinkingFileEntity.getManageNumber())
                                            .build());
                    // WMS連携ステータスを更新(処理前に戻す)
                    directDistributionShipmentConfirmFileLinkingScheduleComponent.updateWmsLinkingStatus(
                            WmsLinkingStatusType.FILE_NOT_CREATE,
                            wmsLinkingFileEntity, userId);

                    continue;
                }

                try {
                    // CSVファイル名(秒数)が重複しないよう1秒遅延する
                    TimeUnit.SECONDS.sleep(1);
                    directDistributionShipmentConfirmFileLinkingScheduleComponent.executeByWmsLinkingFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // WMS連携ステータスを更新(ファイル作成エラー)
                    directDistributionShipmentConfirmFileLinkingScheduleComponent.updateWmsLinkingStatus(
                            WmsLinkingStatusType.FILE_CREATE_ERROR,
                            wmsLinkingFileEntity, userId);
                }

                log.info(LogStringUtil.of("executeByWmsLinkingFile")
                        .message("End processing of DirectDistributionShipmentConfirmFileLinkingSchedule.")
                           .build());
            }

            // 一時保存したファイルを削除
            directDistributionShipmentConfirmLinkingCreateCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
