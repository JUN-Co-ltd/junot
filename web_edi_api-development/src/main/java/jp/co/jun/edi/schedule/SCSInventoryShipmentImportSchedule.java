package jp.co.jun.edi.schedule;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.SCSInventoryShipmentInstructionFileComponent;
import jp.co.jun.edi.component.schedule.SCSInventoryShipmentImportScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * SCS・ZOZO在庫出荷指示関連ファイル連携スケジュール.
 * ・SCS・ZOZO在庫出荷指示CSVファイル(取寄データ)をS3よりダウンロード
 * ・SCS・ZOZO在庫出荷指示CSVファイル(取寄データ)を取込
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
+ ".schedule.scs-inventory-shipment-import-schedule.enabled", matchIfMissing = true)
public class SCSInventoryShipmentImportSchedule  {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.scs-inventory-shipment-import-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private SCSInventoryShipmentImportScheduleComponent scsInventoryShipmentImportScheduleComponent;

    @Autowired
    private SCSInventoryShipmentInstructionFileComponent scsInventoryShipmentInstructionFileComponent;

    /**
     * SCS・ZOZO在庫出荷指示CSVファイル(取寄データ)取込実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.INVENTORY_IMPORT,
                        WmsLinkingStatusType.FILE_NOT_IMPORT,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));

        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // WMS連携ステータスを更新(ファイル取込中)
            scsInventoryShipmentImportScheduleComponent.updateWmsLinkingStatusFileImporting(wmsLinkingFiles, userId);
            // CSVファイル取込
            execute(wmsLinkingFiles, userId);

        }
    }

    /**
     * SCS・ZOZO在庫出荷指示CSVファイル(取寄データ)取込の実行.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void execute(final List<TWmsLinkingFileEntity> wmsLinkingFiles, final BigInteger userId) {
        try {

            // SCS・ZOZO在庫出荷指示CSVファイル(取寄データ)ごとにスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {
                log.info(LogStringUtil.of("executeBySCSInventoryShipmentInstructionFile")
                        .message("Start processing of SCSInventoryShipmentImportSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("s3_key", wmsLinkingFileEntity.getS3Key())
                        .build());

                try {
                    scsInventoryShipmentImportScheduleComponent.executeByInventoryConfirmFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // エラーが発生した場合は、WMS連携ステータスを更新(ファイル取込エラー)
                    scsInventoryShipmentImportScheduleComponent.updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORT_ERROR, wmsLinkingFileEntity, userId);
                }

                log.info(LogStringUtil.of("executeBySCSInventoryShipmentInstructionFile")
                        .message("End processing of SCSInventoryShipmentImportSchedule.")
                        .build());
            }

            // 一時保存したファイルを削除
            scsInventoryShipmentInstructionFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
