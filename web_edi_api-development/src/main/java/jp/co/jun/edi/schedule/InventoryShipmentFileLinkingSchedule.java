package jp.co.jun.edi.schedule;
import java.math.BigInteger;
import java.util.Arrays;
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

import jp.co.jun.edi.component.InventoryShipmentLinkingCreateCsvFileComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.schedule.InventoryShipmentFileLinkingScheduleComponent;
import jp.co.jun.edi.component.schedule.ScheduleBusinessComponent;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.TInventoryShipmentEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TInventoryShipmentRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;


/**
 * 在庫出荷ファイル作成スケジュール.
 * ・ CSVファイルをS3へアップロード
 */
@Slf4j
@Component
@ConditionalOnProperty(value = PropertyName.ROOT
+ ".schedule.inventory-shipment-file-linking-schedule.enabled", matchIfMissing = true)
public class InventoryShipmentFileLinkingSchedule {
    private static final String PROPERTY_NAME_PREFIX =
            PropertyName.ROOT + ".schedule.inventory-shipment-file-linking-schedule";
    private static final String PROPERTY_NAME_CRON = "${" + PROPERTY_NAME_PREFIX + ".cron}";

    @Autowired
    private InventoryShipmentLinkingCreateCsvFileComponent inventoryShipmentLinkingCreateCsvFileComponent;

    @Autowired
    private InventoryShipmentFileLinkingScheduleComponent inventoryShipmentFileLinkingScheduleComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private TInventoryShipmentRepository tInventoryShipmentRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    /**
     * 在庫出荷ファイル連携実行.
     */
    @Scheduled(cron = PROPERTY_NAME_CRON)
    public void task() {
        // 最大処理件数：50
        final Integer maxProcesses = propertyComponent.getBatchProperty().getShipmentProperty().getShipmentMaxProcesses();

        // 倉庫連携ファイル情報リスト取得
        final Page<TWmsLinkingFileEntity> wmsLinkingFilePages =
                tWmsLinkingFileRepository.findByBusinessTypeAndWmsLinkingStatusType(
                        BusinessType.INVENTORY_INSTRUCTION,
                        WmsLinkingStatusType.FILE_NOT_CREATE,
                        PageRequest.of(0, maxProcesses, Sort.by(Order.asc("createdAt"))));

        if (wmsLinkingFilePages.hasContent()) {
            final List<TWmsLinkingFileEntity> wmsLinkingFiles = wmsLinkingFilePages.getContent();
            // ユーザID取得
            final BigInteger userId = scheduleBusinessComponent.getUserId();

            // WMS連携ステータスを更新(ファイル作成中)
            inventoryShipmentFileLinkingScheduleComponent.updateWmsLinkingStatusFileCreating(wmsLinkingFiles, userId);
            // CSVファイル作成
            execute(wmsLinkingFiles, userId);
        }
    }

    /**
     * 在庫出荷データファイル作成の実行.
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
                        .message("Start processing of InventoryShipmentFileCreateSchedule.")
                        .value("business_type", wmsLinkingFileEntity.getBusinessType())
                        .value("id", wmsLinkingFileEntity.getId())
                        .value("manage_number", wmsLinkingFileEntity.getManageNumber())
                        .build());

                try {
                    // CSVファイル名(秒数)が重複しないよう1秒遅延する
                    TimeUnit.SECONDS.sleep(1);
                    inventoryShipmentFileLinkingScheduleComponent.executeByWmsLinkingFile(wmsLinkingFileEntity, userId);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    // WMS連携ステータスを更新(ファイル作成エラー)
                    inventoryShipmentFileLinkingScheduleComponent.updateWmsLinkingStatus(
                            WmsLinkingStatusType.FILE_CREATE_ERROR,
                            wmsLinkingFileEntity, userId);
                }

                // 特殊店舗コードから通常店舗コードに変更
                findShopCode(wmsLinkingFileEntity.getId());

                log.info(LogStringUtil.of("executeByWmsLinkingFile")
                        .message("End processing of InventoryShipmentFileCreateSchedule.")
                        .build());
            }

            // 一時保存したファイルを削除
            inventoryShipmentLinkingCreateCsvFileComponent.deleteFiles();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     *特殊店舗コードから通常店舗コードに変更.
     * @param id wmsLinkingFileId
     */
    private void findShopCode(final BigInteger id) {

        // 更新対象の店舗コードを取得
        final List<TInventoryShipmentEntity> inventoryShipmentEntity = tInventoryShipmentRepository.
                findByManageNumberAndWmsLinkingFileId(id);

        for (final TInventoryShipmentEntity ise : inventoryShipmentEntity) {

            final MCodmstEntity codmst = mCodmstRepository.findByTblidAndCode1sAndItem1(
                    MCodmstTblIdType.CONVERSION_SHOP_CODE.getValue(),
                    Arrays.asList(StringUtils.toStringPadding0(ise.getInstructorSystem().getValue(), 2)),
                    ise.getShopCode()).orElse(new MCodmstEntity());

            if (codmst.getCode2() == null) {
                // 変換に失敗した場合は、ログを出力して次のレコードの処理を続行
                log.warn(LogStringUtil.of("findShopCode")
                        .message("shop code not found.")
                        .value("shop_code", ise.getShopCode())
                        .value("id", ise.getId())
                        .value("wms_linking_file_id", ise.getWmsLinkingFileId())
                        .value("manage_number", ise.getManageNumber())
                        .build());
            } else {
                ise.setShopCode(codmst.getCode2());
                tInventoryShipmentRepository.save(ise);
            }
        }
    }
}
