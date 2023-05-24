package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.ReplenishmentShippingInstructionLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.model.ReplenishmentShippingInstructionLinkingImportCsvModel;
import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.entity.TInventoryShipmentEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.WReplenishmentShippingInstructionEntity;
import jp.co.jun.edi.entity.extended.ExtendedWReplenishmentShippingInstructionEntity;
import jp.co.jun.edi.entity.key.WReplenishmentShippingInstructionKey;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TInventoryShipmentRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.repository.WReplenishmentShippingInstructionRepository;
import jp.co.jun.edi.repository.extended.ExtendedWReplenishmentShippingInstructionRepository;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.ShippingCategoryType;
import jp.co.jun.edi.type.ShippingInstructionDataType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 補充出荷指示データファイル取込スケジュールのコンポーネント.
 */
@Component
public class ReplenishmentShippingInstructionFileImportScheduleComponent {

    @Autowired
    private ReplenishmentShippingInstructionLinkingImportCsvFileComponent linkingImportCsvFileComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private WReplenishmentShippingInstructionRepository insertRepository;

    @Autowired
    private TInventoryShipmentRepository insertInventoryShipmentRepository;

    @Autowired
    private ExtendedWReplenishmentShippingInstructionRepository extendedInsertInventoryShipmentRepository;

    /** CSV設定. */
    private static final CSVFormat CSV_FORMAT =
            CSVFormat.DEFAULT // デフォルトのCSV形式を指定
            .withIgnoreEmptyLines(false) // 空行を無視する
            .withIgnoreSurroundingSpaces(true) // 値をtrimして取得する
            .withRecordSeparator("\r\n") // 改行コードCRLF
            .withDelimiter(',') // 区切りカンマ
            .withEscape(';') // エスケープ文字ダブルクォート以外
            .withQuoteMode(QuoteMode.NONE); // 囲み文字なし

    /**
     * 補充出荷指示CSVファイルごとに処理実行.
     * ・S3よりCSVファイルをダウンロード
     * ・CSVファイルの読み込み
     * ・出荷関連テーブルの更新
     * ・倉庫連携ファイル情報の更新
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeByConfirmFile(final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) throws Exception {
        // 補充出荷指示CSVファイルをS3よりダウンロード
        final File confirmFile = linkingImportCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // CSVファイルを読み込み、Modelに変換
        final List<ReplenishmentShippingInstructionLinkingImportCsvModel> importCsvModels = linkingImportCsvFileComponent
                .readCsvData(confirmFile, CSV_FORMAT, CharsetsConstants.MS932);

        // importCsvModelsが空の場合は取込エラー
        if (CollectionUtils.isEmpty(importCsvModels)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                    .message("confirm file no data.")
                    .value("s3_key", wmsLinkingFileEntity.getS3Key())
                    .build()));
        }

        // DB更新
        insertData(importCsvModels, userId);

        // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
        updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
    }

    /**
     * 補充出荷指示関連のデータ更新.
     *
     * @param models 補充出荷指示データのリスト
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    private void insertData(final List<ReplenishmentShippingInstructionLinkingImportCsvModel> models,
            final BigInteger userId) throws Exception {
        // 補充出荷指示データの登録
        insertReplenishmentShippingInstruction(models, userId);

        // 在庫出荷情報を登録
        insertInventoryShipment(userId);
    }

    /**
     * 補充出荷指示情報登録.
     *
     * @param models 補充出荷指示データのリスト
     * @param updatedUserId 更新ユーザID
     * @throws Exception 例外
     */
    private void insertReplenishmentShippingInstruction(
            final List<ReplenishmentShippingInstructionLinkingImportCsvModel> models,
            final BigInteger updatedUserId) throws Exception {
        // 補充出荷指示情報リスト→テーブルに登録
        List<WReplenishmentShippingInstructionEntity> entities = models.stream()
                .map(model -> createEntity(model, updatedUserId))
                .collect(Collectors.toList());

        // レコード追加
        insertRepository.saveAll(entities);

    }

    /**
     * レコードを生成.
     *
     * @param model 補充出荷指示データ
     * @param updatedUserId ユーザID
     * @return 補充出荷指示情報
     */
    private WReplenishmentShippingInstructionEntity createEntity(
            final ReplenishmentShippingInstructionLinkingImportCsvModel model,
            final BigInteger updatedUserId
            ) {
        WReplenishmentShippingInstructionEntity entity = new WReplenishmentShippingInstructionEntity();
        BeanUtils.copyProperties(model, entity);

        entity.setDataType(ShippingInstructionDataType.convertToType(model.getDataType()));
        entity.setShippingInstructionsLot(NumberUtils.createInteger(model.getShippingInstructionsLot()));

        entity.setCreatedUserId(updatedUserId);
        entity.setUpdatedUserId(updatedUserId);

        // キー生成
        WReplenishmentShippingInstructionKey key = new WReplenishmentShippingInstructionKey();
        key.setManageDate(DateUtils.stringToDate(model.getManageDate()));
        key.setManageAt(DateUtils.stringToTime(model.getManageAt()));
        key.setSequence(NumberUtils.createInteger(model.getSequence()));
        entity.setWReplenishmentShippingInstructionKey(key);

        return entity;
    }

    /**
     * 補充出荷指示情報ワーク登録.
     *
     * @param updatedUserId 更新ユーザID
     * @throws Exception 例外
     */
    private void insertInventoryShipment(
            final BigInteger updatedUserId) throws Exception {

        final long count = insertRepository.count();

        List<ExtendedWReplenishmentShippingInstructionEntity> orgEntities
        = extendedInsertInventoryShipmentRepository.findAllFromWReplenishmentShippingInstruction();

        // 補充出荷指示情報リスト→不足パラメータを補完してテーブルに登録
        List<TInventoryShipmentEntity> entities = orgEntities.stream()
                .map(model -> createInventoryShipmentEntity(model, updatedUserId))
                .collect(Collectors.toList());

        // 補充出荷指示情報リストの件数と一致しない場合はエラー
        if (entities.size() != count) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("insertInventoryShipment")
                    .message("unmatch TInventoryShipment and extendedInsertInventoryShipment.")
                    .value("TInventoryShipment count", count)
                    .value("extendedInsertInventoryShipment count", entities.size())
                    .build()));
        }

        // レコード追加
        insertInventoryShipmentRepository.saveAll(entities);
    }

    /**
     * レコードを生成.
     *
     * @param org 拡張補充出荷指示ワーク情報
     * @param updatedUserId ユーザID
     * @return 補充出荷指示情報
     */
    private TInventoryShipmentEntity createInventoryShipmentEntity(
            final ExtendedWReplenishmentShippingInstructionEntity org,
            final BigInteger updatedUserId
            ) {
        TInventoryShipmentEntity entity = new TInventoryShipmentEntity();
        BeanUtils.copyProperties(org, entity);

        // 不足項目を入力
        entity.setCargoAt(DateUtils.stringToDate(org.getCargoAt()));

        Date dt = DateUtils.createNow();
        entity.setInstructionManageUserDate(dt);
        entity.setInstructionManageUserAt(dt);

        entity.setCreatedUserId(updatedUserId);
        entity.setUpdatedUserId(updatedUserId);

        if (entity.getShippingCategory() != ShippingCategoryType.ALLOCATION_STORE) {
            // 2以外の時は設定しない(2のみ指定→2以外はnullに戻す)
            entity.setInstructionManageShopCode(null);
        } else if (entity.getShippingCategory() != ShippingCategoryType.ALLOCATION_PRODUCT) {
            // 1以外の時は設定しない
            entity.setInstructionManagePartNo(null);
            entity.setInstructionManageDivisionCode(null);
        }
        entity.setLgSendType(LgSendType.NO_INSTRUCTION);
        return entity;
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

    /**
     * 補充出荷指示ワークテーブル truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void truncateTable() {
        // ワークテーブルをtruncateする
        insertRepository.truncateTable();
    }

}
