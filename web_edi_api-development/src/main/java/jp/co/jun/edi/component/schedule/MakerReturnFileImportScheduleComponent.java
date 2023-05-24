package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.MakerReturnLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.model.MakerReturnLinkingImportCsvModel;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.CollectionUtils;
//import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * メーカー返品確定データファイル取込スケジュールのコンポーネント.
 */
@Component
public class MakerReturnFileImportScheduleComponent {

    @Autowired
    private MakerReturnLinkingImportCsvFileComponent linkingImportCsvFileComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TMakerReturnRepository makerReturnRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    /**
     * メーカー返品確定CSVファイルごとに処理実行.
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
    public void executeByMakerReturnConfirmFile(final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final BigInteger userId) throws Exception {

        // メーカー返品確定CSVファイルをS3よりダウンロード
        final File makerReturnConfirmFile = linkingImportCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // CSVファイルを読み込み、Modelに変換
        final List<MakerReturnLinkingImportCsvModel> importCsvModels = linkingImportCsvFileComponent
                .readCsvData(makerReturnConfirmFile);

        // importCsvModelsが空の場合は取込エラー
        if (CollectionUtils.isEmpty(importCsvModels)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                    .message("confirm file no data.")
                    .value("s3_key", wmsLinkingFileEntity.getS3Key())
                    .build()));
        }

        // DB更新
        updateConfirmData(importCsvModels, userId);

        // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
        updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
    }

    /**
     * メーカー返品確定関連のデータ更新.
     * ・仕入情報 t_purchase
     * ・納品明細情報 t_delivery_detail
     * ・納品SKU t_delivery_sku
     * ・発注情報 t_order
     * ・発注SKU t_order_sku
     *
     * @param models 確定データのリスト
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    private void updateConfirmData(final List<MakerReturnLinkingImportCsvModel> models,
            final BigInteger userId) throws Exception {

        for (final MakerReturnLinkingImportCsvModel importModel : models) {
            // メーカー返品情報更新
            final TMakerReturnEntity makerReturnEntity = updateTMakerReturn(importModel, userId);
            // 発注SKU情報更新
            updateTOrderSku(importModel, makerReturnEntity, userId);
        }
    }

    /**
     * メーカー返品情報更新.
     *
     * @param model 確定データ
     * @param updatedUserId ID
     * @return 更新情報
     * @throws Exception Exception
     */
    private TMakerReturnEntity updateTMakerReturn(
            final MakerReturnLinkingImportCsvModel model,
            final BigInteger updatedUserId) throws Exception {

//      #6783 mod JFE start
        // メーカー返品情報取得。取得できない場合はエラー
        final TMakerReturnEntity makerReturnEntity =
                makerReturnRepository.findByManageColumnAndSequence(
                        model.getVoucherNumber(),
                        NumberUtils.createInteger(model.getVoucherLine()))
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getMakerReturnInfo")
                        .message("t_maker_return not found.")
                        .build())));
//      #6783 mod JFE end

        // 返品確定数、更新ユーザーIDをセット
        makerReturnEntity.setFixReturnLot(NumberUtils.createInteger(model.getFixArrivalCount()));
        makerReturnEntity.setUpdatedUserId(updatedUserId);
        makerReturnRepository.save(makerReturnEntity);
        return makerReturnEntity;
    }

    /**
     * 発注SKU情報更新.
     *
     * @param model 確定データ
     * @param makerReturnEntity メーカー返品情報
     * @param updatedUserId ID
     * @throws Exception Exception
     */
    private void updateTOrderSku(
            final MakerReturnLinkingImportCsvModel model,
            final TMakerReturnEntity makerReturnEntity,
            final BigInteger updatedUserId) throws Exception {
        final int unitPrice =
                orderRepository.findByOrderIdGetUnitPrice(makerReturnEntity.getOrderId());
        final TOrderSkuEntity orderSkuEntity =
                orderSkuRepository.findByOrderIdAndColorAndSizeWithTOrderCheck(
                        makerReturnEntity.getOrderId(),
                        makerReturnEntity.getColorCode(),
                        makerReturnEntity.getSize())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getOrderSkuInfo")
                        .message("t_order_sku not found.")
                        .build())));

        // 入荷数量
        final int fixCount = NumberUtils.createInteger(model.getFixArrivalCount()).intValue();

        // 発注情報.単価 × 入荷数量
        final int price = unitPrice * fixCount;

        orderSkuEntity.setDeliveryLot(orderSkuEntity.getDeliveryLot() - fixCount);
        orderSkuEntity.setArrivalLot(orderSkuEntity.getArrivalLot() - fixCount);
        orderSkuEntity.setReturnLot(orderSkuEntity.getReturnLot() + fixCount);
        orderSkuEntity.setNetPurchaseLot(orderSkuEntity.getNetPurchaseLot() - fixCount);
        orderSkuEntity.setNetPurchaseDepositAmount(orderSkuEntity.getNetPurchaseDepositAmount() - price);

        orderSkuRepository.save(orderSkuEntity);
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
