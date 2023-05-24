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

import jp.co.jun.edi.component.DirectPurchaseConfirmLinkingCreateCsvFileComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.DirectPurchaseConfirmFileLinkingScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 直送仕入確定ファイル作成スケジュール.
 * ・ CSVファイルをS3へアップロード
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
+ ".schedule.direct-purchase-confirm-file-linking-schedule.enabled", matchIfMissing = true)
public class DirectPurchaseConfirmFileLinkingSchedule {

    private static final String PROPERTY_NAME_PREFIX =
            PropertyName.ROOT + ".schedule.direct-purchase-confirm-file-linking-schedule";
    private static final String PROPERTY_NAME_CRON =
            "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private DirectPurchaseConfirmLinkingCreateCsvFileComponent directPurchaseConfirmLinkingCreateCsvFileComponent;

    @Autowired
    private DirectPurchaseConfirmFileLinkingScheduleComponent directPurchaseConfirmFileLinkingScheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    /**
     * 直送仕入確定ファイル連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.DIRECT_PURCHASE_CONFIRM,
                        WmsLinkingStatusType.FILE_NOT_CREATE,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));

        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // WMS連携ステータスを更新(ファイル作成中)
            directPurchaseConfirmFileLinkingScheduleComponent.updateWmsLinkingStatusFileCreating(wmsLinkingFiles, userId);
            // CSVファイル作成
            execute(wmsLinkingFiles, userId);
        }
    }

    /**
     * 直送仕入確定ファイル作成の実行.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param userId システムユーザーID
     */
    public void execute(final List<TWmsLinkingFileEntity> wmsLinkingFiles,
            final BigInteger userId) {
        try {
            // 倉庫連携ファイル情報毎にスケジュール実行
            for (final TWmsLinkingFileEntity wmsLinkingFileEntity : wmsLinkingFiles) {
                log.info(LogStringUtil.of("executeByWmsLinkingFile")
                        .message("Start processing of DirectPurchaseConfirmFileLinkingSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("id", wmsLinkingFileEntity.getId())
                        .value("manage_number", wmsLinkingFileEntity.getManageNumber())
                        .build());

                try {
                    // CSVファイル名(秒数)が重複しないよう1秒遅延する
                    TimeUnit.SECONDS.sleep(1);
                    directPurchaseConfirmFileLinkingScheduleComponent.executeByWmsLinkingFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // WMS連携ステータスを更新(ファイル作成エラー)
                    directPurchaseConfirmFileLinkingScheduleComponent.updateWmsLinkingStatus(
                            WmsLinkingStatusType.FILE_CREATE_ERROR,
                            wmsLinkingFileEntity, userId);
                }

                log.info(LogStringUtil.of("executeByWmsLinkingFile")
                        .message("End processing of DirectPurchaseConfirmFileLinkingSchedule.")
                        .build());
            }

            // 一時保存したファイルを削除
            directPurchaseConfirmLinkingCreateCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
