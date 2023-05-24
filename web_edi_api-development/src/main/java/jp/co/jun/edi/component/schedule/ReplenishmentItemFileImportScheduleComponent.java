package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
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

import jp.co.jun.edi.component.ReplenishmentItemLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.model.ReplenishmentItemLinkingImportCsvModel;
import jp.co.jun.edi.constants.CharsetsConstants;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.WReplenishmentItemEntity;
import jp.co.jun.edi.entity.key.WReplenishmentItemKey;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.repository.WReplenishmentItemRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 補充対象品番データファイル取込スケジュールのコンポーネント.
 */
@Component
public class ReplenishmentItemFileImportScheduleComponent {

    @Autowired
    private ReplenishmentItemLinkingImportCsvFileComponent linkingImportCsvFileComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private WReplenishmentItemRepository insertRepository;

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
     * 補充対象品番CSVファイルごとに処理実行.
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
        // 補充対象品番CSVファイルをS3よりダウンロード
        final File confirmFile = linkingImportCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // CSVファイルを読み込み、Modelに変換
        final List<ReplenishmentItemLinkingImportCsvModel> importCsvModels = linkingImportCsvFileComponent
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
     * 補充対象品番関連のデータ更新.
     *
     * @param models 補充対象品番データのリスト
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    private void insertData(final List<ReplenishmentItemLinkingImportCsvModel> models,
            final BigInteger userId) throws Exception {
        // 情報の作成
        insertReplenishmentItem(models, userId);
    }

    /**
     * 補充対象品番情報登録.
     *
     * @param models 補充対象品番データのリスト
     * @param updatedUserId 更新ユーザID
     * @throws Exception 例外
     */
    private void insertReplenishmentItem(
            final List<ReplenishmentItemLinkingImportCsvModel> models,
            final BigInteger updatedUserId) throws Exception {
        // 補充対象品番情報リスト→テーブルに登録
        List<WReplenishmentItemEntity> entities = models.stream()
                .map(model -> createEntity(model, updatedUserId))
                .collect(Collectors.toList());
        // レコード追加
        insertRepository.saveAll(entities);
    }

    /**
     * レコードを生成.
     *
     * @param model 補充対象品番データ
     * @param updatedUserId ユーザID
     * @return 補充対象品番情報
     */
    private WReplenishmentItemEntity createEntity(
            final ReplenishmentItemLinkingImportCsvModel model,
            final BigInteger updatedUserId
            ) {
        WReplenishmentItemEntity entity = new WReplenishmentItemEntity();
        BeanUtils.copyProperties(model, entity);

        entity.setCreatedUserId(updatedUserId);
        entity.setUpdatedUserId(updatedUserId);

        // キー生成
        WReplenishmentItemKey key = new WReplenishmentItemKey();
        key.setManageDate(DateUtils.stringToDate(model.getManageDate()));
        key.setManageAt(DateUtils.stringToTime(model.getManageAt()));
        key.setSequence(NumberUtils.createInteger(model.getSequence()));
        entity.setWReplenishmentItemKey(key);

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
     * 補充対象品番ワークテーブル truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void truncateTable() {
        // ワークテーブルをtruncateする
        insertRepository.truncateTable();
    }
}
