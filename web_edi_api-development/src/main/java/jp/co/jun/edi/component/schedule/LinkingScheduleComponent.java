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

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.entity.schedule.LinkingCreateCsvFileCommonEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * 指示データファイル作成スケジュールの共通コンポーネント.
 * @param <T>
 */
@Component
public abstract class LinkingScheduleComponent<T extends LinkingCreateCsvFileCommonEntity> {

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private S3Component s3Component;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    private static final String CONTENT_TYPE = "text/csv";

    /**
     * 倉庫連携ファイ時情報毎に処理実行.
     * ・CSVファイルの作成
     * ・S3へのアップロード
     * ・倉庫連携ファイル情報の更新
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeByWmsLinkingFile(final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) throws Exception {

        // 倉庫連携ファイルIDを基にファイル作成モデルを取得
        final List<T> entities = generateInfo(wmsLinkingFileEntity.getId());

        // CSVファイル生成
        final File instructionFile = createInstructionFile(entities);

        // S3プレフィックスを取得する：shipment/[XX]
        final String s3Prefix =
                propertyComponent.getBatchProperty().getShipmentProperty().getShipmentS3Prefix()
                + getBusinessType().getValue();

        // S3へアップロード
        final String s3Key = uploadFileToS3(instructionFile, s3Prefix);

        // 倉庫連携ファイル情報更新
        updateWmsLinkingFile(wmsLinkingFileEntity, instructionFile, s3Key, s3Prefix, userId);
    }

    /**
     * 倉庫連携ファイル情報更新処理実行.
     * 以下項目更新
     *・ファイル名
     *・S3キー
     *・S3プレフィックス
     *・ファイル作成日時
     *・WMS連携ステータス
     *・更新ユーザーID
     *
     * @param wmsLinkingFileEntity 倉庫連携ファイル情報
     * @param instructionFile 指示データファイル
     * @param s3Key S3キー
     * @param s3Prefix S3プレフィックス
     * @param updatedUserId 更新ユーザID
     */
    private void updateWmsLinkingFile(
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final File instructionFile,
            final String s3Key,
            final String s3Prefix,
            final BigInteger updatedUserId) {
        wmsLinkingFileEntity.setFileName(instructionFile.getName());
        wmsLinkingFileEntity.setS3Key(s3Key);
        wmsLinkingFileEntity.setS3Prefix(s3Prefix);
        wmsLinkingFileEntity.setFileCreatedAt(DateUtils.createNow());
        wmsLinkingFileEntity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_CREATED);
        wmsLinkingFileEntity.setUpdatedUserId(updatedUserId);
        tWmsLinkingFileRepository.save(wmsLinkingFileEntity);
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
    public int updateWmsLinkingStatus(
            final WmsLinkingStatusType type,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger updatedUserId) {
        return tWmsLinkingFileRepository.updateWmsLinkingStatus(type, wmsLinkingFileEntity.getId(), updatedUserId);
    }

    /**
     * 倉庫連携ファイル情報のWMS連携ステータスをファイル作成中に更新する.
     *
     * @param wmsLinkingFiles 倉庫連携ファイル情報リスト
     * @param updatedUserId 更新ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateWmsLinkingStatusFileCreating(
            final List<TWmsLinkingFileEntity> wmsLinkingFiles,
            final BigInteger updatedUserId) {
        final List<BigInteger> ids = wmsLinkingFiles.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tWmsLinkingFileRepository.updateWmsLinkingStatusByIds(WmsLinkingStatusType.FILE_CREATING, ids, updatedUserId);
    }

    /**
     * バッチ処理対象のファイル作成データを取得する.
     *
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return ファイル作成Modelリスト
     * @throws Exception 例外
     */
    private List<T> generateInfo(final BigInteger wmsLinkingFileId)
            throws Exception {
        // 倉庫連携ファイルIDを基に指示ファイル作成Modelを取得
        final List<T> entities =
                generateByWmsLinkingFileIdEntities(wmsLinkingFileId);

        // 取得できなかった場合はファイル作成エラー
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("generateInfo")
                    .message("entities not found.")
                    .value("wmsLinkingFileId", wmsLinkingFileId)
                    .build()));
        }

        // チェック内容の拡張条件
        appendValidateCheck(entities, wmsLinkingFileId);

        return entities;
    }

    /**
     * 業務区分を返す.
     *
     * @return 業務区分
     */
    abstract BusinessType getBusinessType();

    /**
     * 倉庫連携ファイル情報IDをキーとしてそれぞれのテーブルの倉庫連携情報を取得する.
     *
     * @param wmsLinkingFileId 倉庫連携ファイル情報ID
     * @return 取得結果一覧
     */
    abstract List<T> generateByWmsLinkingFileIdEntities(BigInteger wmsLinkingFileId);

    /**
     * 追加のチェック.
     *
     * @param entities 指示ファイル作成Modelリスト
     * @param wmsLinkingFileId 倉庫連携ファイルID
     */
    abstract void appendValidateCheck(List<T> entities, BigInteger wmsLinkingFileId);

    /**
     * CSVファイルを作成する.
     *
     * @param entities 指示ファイル作成Modelリスト
     * @return ファイル書込情報
     * @throws Exception 例外
     */
    abstract File createInstructionFile(List<T> entities) throws Exception;

    /**
     * ファイルをS3へアップロード.
     *
     * @param instructionFile 指示データファイル
     * @param s3Prefix S3プレフィックス
     * @return S3キー
     * @throws Exception 例外
     */
    private String uploadFileToS3(
            final File instructionFile,
            final String s3Prefix) throws Exception {
        final String s3Key = s3Component.upload(instructionFile, s3Prefix, CONTENT_TYPE);
        return s3Key;
    }
}
