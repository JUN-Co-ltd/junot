package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.InventoryShipmentLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.model.InventoryShipmentLinkingImportCsvModel;
import jp.co.jun.edi.entity.TInventoryShipmentEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TInventoryShipmentRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 在庫出荷確定データファイル取込スケジュールのコンポーネント.
 */
@Component
public class InventoryShipmentFileImportScheduleComponent {

    @Autowired
    private InventoryShipmentLinkingImportCsvFileComponent importCsvFileComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TInventoryShipmentRepository inventoryShipmentRepository;

  /**
   * 在庫出荷確定CSVファイルごとに処理実行.
   * ・S3よりCSVファイルをダウンロード
   * ・CSVファイルの読み込み
   * ・在庫出荷情報テーブルの更新
   * ・倉庫連携ファイル情報の更新
   *
   * @param wmsLinkingFileEntity 倉庫連携ファイル情報
   * @param userId システムユーザID
   * @throws Exception 例外
   */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeByPurchaseConfirmFile(final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) throws Exception {
        // 在庫出荷確定CSVファイルをS3よりダウンロード
        final File importFile = importCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // CSVファイルを読み込み、Modelに変換
        final List<InventoryShipmentLinkingImportCsvModel> importCsvModels = importCsvFileComponent.readCsvData(importFile);

        // importCsvModelsが空の場合は取込エラー
        if (CollectionUtils.isEmpty(importCsvModels)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                            .message("inventoryShipment confirm file no data.")
                            .value("s3_key", wmsLinkingFileEntity.getS3Key())
                            .build()));
        }

        // DB更新
        updateConfirmData(importCsvModels, userId);

        // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
        updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
    }

    /**
     * 在庫出荷確定関連のデータ更新.
     * ・在庫出荷情報 t_inventory_shipment
     *
     * @param importCsvModels 在庫出荷確定データのリスト
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    private void updateConfirmData(final List<InventoryShipmentLinkingImportCsvModel> importCsvModels,
            final BigInteger userId) throws Exception {
        importCsvModels.forEach(csvModel -> updateInventoryShipment(csvModel, userId));
    }

    /**
     * 在庫出荷指示データ更新.
     * @param model 在庫出荷確定データ
     * @param userId システムユーザID* @param userId システムユーザID
     */
    private void updateInventoryShipment(final InventoryShipmentLinkingImportCsvModel model,
                                          final BigInteger userId) {

        final TInventoryShipmentEntity entity = inventoryShipmentRepository.findByManageColumnAndSequence(
                model.getPartNo(),
                model.getColorCode(),
                model.getSize(),
                model.getTnpCode(),
                model.getInstructNumber(),
                NumberUtils.createInteger(model.getInstructNumberLine())
            )
            .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("getInventoryShipment")
                    .message("t_inventory_shipment not found.")
                    .build())));

        // 出荷伝票No
        entity.setShipmentVoucherNumber(model.getShipmentVoucherNumber());
        // 出荷伝票行
        entity.setShipmentVoucherLine(NumberUtils.createInteger(model.getShipmentVoucherLine()));
        // 出荷確定数
        entity.setFixShippingInstructionLot(NumberUtils.createInteger(model.getFixShippingInstructionLot()));
        // 更新ユーザID
        entity.setUpdatedUserId(userId);

        inventoryShipmentRepository.save(entity);
    }

    /**
     * 倉庫連携ファイル情報のWMS連携ステータスを更新する.
     *
     * @param type WMS連携ステータス
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int updateWmsLinkingStatus(final WmsLinkingStatusType type,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger updatedUserId) {
        return tWmsLinkingFileRepository.updateWmsLinkingStatus(type, wmsLinkingFileEntity.getId(), updatedUserId);
    }

    /**
     * 倉庫連携ファイル情報のWMS連携ステータスをファイル取込中に更新する.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param updatedUserId 更新ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateWmsLinkingStatusFileImporting(final List<TWmsLinkingFileEntity> wmsLinkingFiles,
            final BigInteger updatedUserId) {
        final List<BigInteger> ids = wmsLinkingFiles.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tWmsLinkingFileRepository.updateWmsLinkingStatusByIds(WmsLinkingStatusType.FILE_IMPORTING, ids, updatedUserId);
    }
}
