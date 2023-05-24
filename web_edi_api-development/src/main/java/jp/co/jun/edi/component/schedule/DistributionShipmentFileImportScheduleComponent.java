package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.DistributionShipmentLinkingImportCsvFileComponent;
import jp.co.jun.edi.component.model.DistributionShipmentLinkingImportCsvModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 配分出荷指示確定データファイル取込スケジュールのコンポーネント.
 */
@Component
public class DistributionShipmentFileImportScheduleComponent {

    @Autowired
    private DistributionShipmentLinkingImportCsvFileComponent importCsvFileComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    /**
     * 配分出荷指示確定CSVファイルごとに処理実行.
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

        // 指示確定CSVファイルをS3よりダウンロード
        final File confirmFile =
                importCsvFileComponent.downloadCsvFile(wmsLinkingFileEntity);

        // CSVファイルを読み込み、Modelに変換
        final List<DistributionShipmentLinkingImportCsvModel> models =
                importCsvFileComponent.readCsvData(confirmFile);

        // 空の場合は取込エラー
        if (CollectionUtils.isEmpty(models)) {
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("readCsvData")
                    .message("distribution shipment confirm file no data.")
                    .value("s3_key", wmsLinkingFileEntity.getS3Key())
                    .build()));
        }

        // DB更新
        updateConfirmData(models, userId);

        // 倉庫連携ファイル情報のWMS連携ステータスをファイル取込済に更新する
        updateWmsLinkingStatus(WmsLinkingStatusType.FILE_IMPORTED, wmsLinkingFileEntity, userId);
    }

    /**
     * 配分出荷指示確定関連のデータ更新.
     * ・納品明細情報 t_delivery_detail
     * ・納品得意先SKU t_delivery_store_sku
     *
     * @param models 仕入確定データのリスト
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    private void updateConfirmData(
            final List<DistributionShipmentLinkingImportCsvModel> models,
            final BigInteger userId) throws Exception {
        List<DistributionShipmentLinkingImportCsvModel> detailTempModels =
                new ArrayList<DistributionShipmentLinkingImportCsvModel>();
        for (final DistributionShipmentLinkingImportCsvModel model : models) {
            // 重複削除のため、納品明細更新用のキーリストを作成
            DistributionShipmentLinkingImportCsvModel tempModel
            = new DistributionShipmentLinkingImportCsvModel();
            tempModel.setOrderNumber(model.getOrderNumber());
            tempModel.setDeliveryCount(model.getDeliveryCount());
            tempModel.setDivisionCode(model.getDivisionCode());
            tempModel.setShipmentAt(model.getShipmentAt());
            detailTempModels.add(tempModel);
        }

        // 重複行を除去
        List<DistributionShipmentLinkingImportCsvModel> detailModels
        = new ArrayList<DistributionShipmentLinkingImportCsvModel>(new LinkedHashSet<>(detailTempModels));

        for (final DistributionShipmentLinkingImportCsvModel model : detailModels) {
             //PRD_0108  #7383 JFE add start
             //納品依頼回数を数値に変換。変換できなかった場合は0を返す
            int intDeliveryCount = 0;
            try {
                intDeliveryCount = Integer.parseInt(model.getDeliveryCount());
            }
            catch (NumberFormatException e)
            {
                intDeliveryCount = 0;
            }
             //PRD_0108  #7383 JFE add end

            // 情報取得。取得できない場合はエラー
            final TDeliveryDetailEntity entity =
                    deliveryDetailRepository.findByOrderNumberAndDeliveryCountAndDivisionCode(
                            new BigInteger(model.getOrderNumber()),
                            //PRD_0108  #7383 JFE mod start
                            //NumberUtils.createInteger(model.getDeliveryCount()).intValue(),
                            intDeliveryCount,
                            //PRD_0108  #7383 JFE mod end
                            model.getDivisionCode())
                    .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("getDeliveryDetailInfo")
                            .message("t_delivery_detail not found.")
                            .build())));

            // 発注SKU別の仕入確定データ
            final List<DistributionShipmentLinkingImportCsvModel> importModels =
                    models.stream().filter(p ->
                    Objects.equals(p.getOrderNumber(), model.getOrderNumber())
                    && Objects.equals(p.getDeliveryCount(), model.getDeliveryCount())
                    && Objects.equals(p.getDivisionCode(), model.getDivisionCode()))
                    .collect(Collectors.toList());
            for (final DistributionShipmentLinkingImportCsvModel importModel : importModels) {
                 //PRD_0108  #7383 JFE add start
                 //納品依頼回数を数値に変換。変換できなかった場合は0を返す
                int intDeliveryCountSKU = 0;
                try {
                    intDeliveryCountSKU = Integer.parseInt(importModel.getDeliveryCount());
                }
                catch (NumberFormatException e)
                {
                    intDeliveryCountSKU = 0;
                }
                //PRD_0140 #10512 JFE add start
                final String FinalSizeCode = importModel.getSizeCode();
                final String FinalColorCode = importModel.getColorCode();
                final String FinalTnpCode = importModel.getTnpCode();
                final String FinalDivisionCode = importModel.getDivisionCode();
                final BigInteger FinalOrderNumber = NumberUtils.createBigInteger(importModel.getOrderNumber());
                final int FinalintDeliveryCountSKU = intDeliveryCountSKU;
                final String FinalPartNo = importModel.getPartNo();
                //PRD_0140 #10512 JFE add end

                //PRD_0108  #7383 JFE add end
                // 情報取得。取得できない場合はエラー
                final TDeliveryStoreSkuEntity dssEntity =
                        deliveryStoreSkuRepository.findByManageColumnAndLineNumber(
                                importModel.getSizeCode(),
                                importModel.getColorCode(),
                                importModel.getTnpCode(),
                                importModel.getDivisionCode(),
                                NumberUtils.createBigInteger(importModel.getOrderNumber()),
                              //PRD_0108  #7383 JFE mod start
                                //NumberUtils.createInteger(importModel.getDeliveryCount()),
                                intDeliveryCountSKU,
                              //PRD_0108  #7383 JFE mod end
                                importModel.getPartNo())
                        .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("getDeliveryStoreSkuInfo")
                                .message("t_delivery_store_sku not found.")
                              //PRD_0140 #10512 JFE add start
                                .value("size", FinalSizeCode)
                                .value("colorCode", FinalColorCode)
                                .value("storeCode", FinalTnpCode)
                                .value("divisionCode", FinalDivisionCode)
                                .value("orderNumber", FinalOrderNumber)
                                .value("deliveryCount", FinalintDeliveryCountSKU)
                                .value("partNo", FinalPartNo)
                              //PRD_0140 #10512 JFE add end
                                .build())));
                // 納品得意先SKUの更新
                updateDeliveryStoreSku(dssEntity, importModel, userId);
            }
            // 納品明細情報の更新
            updateDeliveryDetail(entity, model, userId);
        }
    }

    /**
     * 納品明細情報更新.
     * 以下項目更新
     * ・配分完了フラグ
     * ・配分確定フラグ
     * ・配分完了日
     * ・配分計上日
     * ・更新ユーザーID
     *
     * @param entity 納品明細情報
     * @param model 取込データModel
     * @param updatedUserId 更新ユーザID
     * @throws Exception 例外
     */
    private void updateDeliveryDetail(
            final TDeliveryDetailEntity entity,
            final DistributionShipmentLinkingImportCsvModel model,
            final BigInteger updatedUserId) throws Exception {
        // 値再セット
        entity.setAllocationCargoAt(DateUtils.stringToDate(model.getShipmentAt()));
        entity.setAllocationCompleteFlg(BooleanType.TRUE);
        entity.setAllocationConfirmFlg(BooleanType.TRUE);
        entity.setAllocationCompleteAt(DateUtils.createNow());
        entity.setAllocationRecordAt(DateUtils.createNow());
        entity.setUpdatedUserId(updatedUserId);
        deliveryDetailRepository.save(entity);
    }

    /**
     * 納品得意先SKU情報更新.
     * 以下項目更新
     * ・出荷伝票No
     * ・出荷伝票行
     * ・出荷確定数
     *
     * @param entity 納品得意先SKU情報
     * @param model 取込データModel
     * @param updatedUserId 更新ユーザID
     * @throws Exception 例外
     */
    private void updateDeliveryStoreSku(
            final TDeliveryStoreSkuEntity entity,
            final DistributionShipmentLinkingImportCsvModel model,
            final BigInteger updatedUserId) throws Exception {
        // 値再セット
        entity.setShipmentVoucherNumber(model.getShipmentVoucherNumber());
        entity.setShipmentVoucherLine(NumberUtils.createInteger(model.getShipmentVoucherLine()));
        entity.setArrivalLot(NumberUtils.createInteger(model.getAllocationFixLot()));
        entity.setUpdatedUserId(updatedUserId);
        deliveryStoreSkuRepository.save(entity);
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
