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
import jp.co.jun.edi.component.ReplenishmentItemLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.ReplenishmentShippingInstructionLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.schedule.ReplenishmentItemFileImportScheduleComponent;
import jp.co.jun.edi.component.schedule.ReplenishmentShippingInstructionFileImportScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 補充対象品番・補充出荷指示ファイル連携スケジュール.
 * ・補充対象品番・補充出荷指示データCSVファイルをS3よりダウンロード
 * ・補充対象品番・補充出荷指示データを取込
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
+ ".schedule.replenishment-file-import-schedule.enabled", matchIfMissing = true)
public class ReplenishmentFileImportSchedule {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".schedule.replenishment-file-import-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    //-------------------
    // 補充対象品番
    @Autowired
    private ReplenishmentItemLinkingImportCsvFileComponent itemLinkingImportCsvFileComponent;

    @Autowired
    private ReplenishmentItemFileImportScheduleComponent itemFileImportScheduleComponent;

    //-------------------
    // 補充出荷指示
    @Autowired
    private ReplenishmentShippingInstructionLinkingImportCsvFileComponent shippingInstructionLinkingImportCsvFileComponent;

    @Autowired
    private ReplenishmentShippingInstructionFileImportScheduleComponent shippingInstructionFileImportScheduleComponent;

    //-------------------
    // 共通
    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    /**
     * ファイル取込実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 補充対象品番登録
        taskItem(maxProcesses);

        // 補充出荷指示登録
        taskShippingInstruction(maxProcesses);
    }

    /**
     * 補充対象品番ファイル取込実行.
     *
     * @param maxProcesses 最大読込件数
     */
    public void taskItem(final Integer maxProcesses) {
        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.REPLENISHMENT_ITEM,
                        WmsLinkingStatusType.FILE_NOT_IMPORT,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();
            // WMS連携ステータスを更新(ファイル取込中)
            itemFileImportScheduleComponent.updateWmsLinkingStatusFileImporting(wmsLinkingFiles, userId);
            // CSVファイル取込
            executeItem(wmsLinkingFiles, userId);
        }
    }

    /**
     * 補充対象品番データファイル取込の実行.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void executeItem(final List<TWmsLinkingFileEntity> wmsLinkingFiles, final BigInteger userId) {
        try {

            // ワークテーブルをtruncateする
            itemFileImportScheduleComponent.truncateTable();

            // CSVファイルごとにスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {
                log.info(LogStringUtil.of("executeByConfirmFile")
                        .message("Start processing of ReplenishmentItemFileImportSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("s3_key", wmsLinkingFileEntity.getS3Key())
                        .build());
                try {
                    itemFileImportScheduleComponent.executeByConfirmFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // エラーが発生した場合は、WMS連携ステータスを更新(ファイル取込エラー)
                    itemFileImportScheduleComponent.updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORT_ERROR, wmsLinkingFileEntity, userId);
                }
                log.info(LogStringUtil.of("executeByConfirmFile")
                        .message("End processing of ReplenishmentItemFileImportSchedule.")
                        .build());
            }
            // 一時保存したファイルを削除
            itemLinkingImportCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 補充出荷指示ファイル取込実行.
     *
     * @param maxProcesses 最大読込件数
     */
    public void taskShippingInstruction(final Integer maxProcesses) {
        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.REPLENISHMENT_SHIPPING_INSTRUCTION,
                        WmsLinkingStatusType.FILE_NOT_IMPORT,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));
        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();
            // WMS連携ステータスを更新(ファイル取込中)
            shippingInstructionFileImportScheduleComponent.updateWmsLinkingStatusFileImporting(wmsLinkingFiles, userId);
            // CSVファイル取込
            executeShippingInstruction(wmsLinkingFiles, userId);
        }
    }

    /**
     * 補充出荷指示データファイル取込の実行.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void executeShippingInstruction(final List<TWmsLinkingFileEntity> wmsLinkingFiles, final BigInteger userId) {
        try {

            // ワークテーブルをtruncateする
            shippingInstructionFileImportScheduleComponent.truncateTable();

            // CSVファイルごとにスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {
                log.info(LogStringUtil.of("executeByConfirmFile")
                        .message("Start processing of ReplenishmentShippingInstructionFileImportSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("s3_key", wmsLinkingFileEntity.getS3Key())
                        .build());
                try {
                    shippingInstructionFileImportScheduleComponent.executeByConfirmFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // エラーが発生した場合は、WMS連携ステータスを更新(ファイル取込エラー)
                    shippingInstructionFileImportScheduleComponent.updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORT_ERROR, wmsLinkingFileEntity, userId);
                }
                log.info(LogStringUtil.of("executeByConfirmFile")
                        .message("End processing of ReplenishmentShippingInstructionFileImportSchedule.")
                        .build());
            }
            // 一時保存したファイルを削除
            shippingInstructionLinkingImportCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
