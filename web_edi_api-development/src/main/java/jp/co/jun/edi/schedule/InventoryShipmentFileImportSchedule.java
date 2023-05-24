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

import jp.co.jun.edi.component.InventoryShipmentLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.InventoryShipmentFileImportScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 出荷関連ファイル連携スケジュール.
 * ・在庫出荷確定データCSVファイルをS3よりダウンロード
 * ・在庫出荷確定データを取込
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
        + ".schedule.inventory-shipment-file-import-schedule.enabled", matchIfMissing = true)
public class InventoryShipmentFileImportSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.inventory-shipment-file-import-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private InventoryShipmentFileImportScheduleComponent importScheduleComponent;

    @Autowired
    private InventoryShipmentLinkingImportCsvFileComponent importCsvFileComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    /**
     * 在庫出荷確定ファイル取込実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.INVENTORY_CONFIRM,
                        WmsLinkingStatusType.FILE_NOT_IMPORT,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("id"))));

        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // WMS連携ステータスを更新(ファイル取込中)
            importScheduleComponent.updateWmsLinkingStatusFileImporting(wmsLinkingFiles, userId);
            // CSVファイル取込
            execute(wmsLinkingFiles, userId);

        }
    }

    /**
     * 在庫出荷確定データファイル取込の実行.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void execute(final List<TWmsLinkingFileEntity> wmsLinkingFiles, final BigInteger userId) {
        try {

            // 在庫出荷確定CSVファイルごとにスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {
                log.info(LogStringUtil.of("executeInventoryShipmentConfirmFile")
                        .message("Start processing of InventoryShipmentFileImportSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("s3_key", wmsLinkingFileEntity.getS3Key())
                        .build());

                try {
                    importScheduleComponent.executeByPurchaseConfirmFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // エラーが発生した場合は、WMS連携ステータスを更新(ファイル取込エラー)
                    importScheduleComponent.updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORT_ERROR, wmsLinkingFileEntity, userId);
                }

                log.info(LogStringUtil.of("executeByInventoryShipmentConfirmFile")
                        .message("End processing of InventoryShipmentFileImportSchedule.")
                        .build());
            }

            // 一時保存したファイルを削除
            importCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
