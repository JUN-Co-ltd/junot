package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.DistributionShipmentComponent;
import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.component.PurchaseConfirmComponent;
import jp.co.jun.edi.component.model.PurchaseImportModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.TDeliveryVoucherFileInfoEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.TDeliveryVoucherFileInfoRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.DeliveryVoucherCategoryType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 直送確定処理.
 */
@Service
public class DeliveryDirectConfirmService
        extends GenericUpdateService<ApprovalServiceParameter<DeliveryModel>, ApprovalServiceResponse> {
    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private TDeliveryStoreRepository deliveryStoreRepository;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Autowired
    private TDeliveryVoucherFileInfoRepository deliveryVoucherFileInfoRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private PurchaseComponent purchaseComponent;

    @Autowired
    private PurchaseConfirmComponent purchaseConfirmComponent;

    @Autowired
    private DistributionShipmentComponent distributionShipmentComponent;

    @Override
    protected ApprovalServiceResponse execute(final ApprovalServiceParameter<DeliveryModel> serviceParameter) {
        // 納品依頼情報、納品依頼明細情報、納品依頼SKU情報を取得し、DeliveryModelに入れる
        final DeliveryModel deliveryModel = deliveryComponent.findDeliveryById(serviceParameter.getItem().getId());

        // 発注情報取得。存在しない場合はエラー
        final TOrderEntity dbOrder = orderRepository.findByOrderId(deliveryModel.getOrderId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // ユーザーID
        final BigInteger userId = serviceParameter.getLoginUser().getUserId();
        // システム日付
        final Date systemDate = new Date();

        // 仕入の確定
        confirmPurchaseData(deliveryModel, dbOrder, systemDate, userId);

        // 配分出荷の確定
        confirmDistributionShipmentData(deliveryModel, systemDate, userId);

        // 納品伝票ファイル情報登録
        createDeliveryVoucherFileInfo(deliveryModel);

        return ApprovalServiceResponse.builder().build();
    }

    /**
     * 仕入データの確定.
     * (1)仕入情報登録
     * (2)倉庫連携ファイル情報登録(QR)
     * (3)仕入情報更新
     * (4)仕入確定関連データ更新
     *
     * @param deliveryModel 納品依頼情報Model
     * @param dbOrder DB登録済の発注情報
     * @param systemDate システム日付
     * @param userId ユーザーID
     */
    private void confirmPurchaseData(final DeliveryModel deliveryModel,
            final TOrderEntity dbOrder,
            final Date systemDate,
            final BigInteger userId) {
        // 仕入情報の登録
        final List<TPurchaseEntity> directConfirmPurchases = purchaseConfirmComponent.generateDirectConfirmPurchases(
                deliveryModel, dbOrder, systemDate);
        purchaseRepository.saveAll(directConfirmPurchases);

        // 倉庫連携ファイル情報登録(業務区分：QR)
        final TWmsLinkingFileEntity purchaseWms = purchaseComponent.insertWmsLinkingFile(BusinessType.DIRECT_PURCHASE_CONFIRM);

        // 仕入情報の更新(仕入確定済に更新)
        updatePurchases(directConfirmPurchases, purchaseWms, systemDate, userId);

        // 仕入確定関連のデータ更新
        updatePurchaseConfirmData(directConfirmPurchases, systemDate, userId);
    }

    /**
     * 直送確定：仕入情報更新.
     * 仕入確定済に更新.
     *
     * @param dbPurchases DB登録済の仕入情報リスト
     * @param wmsLinkingFileEntity DB登録済の倉庫連携ファイル情報Entity
     * @param systemDate システム日付
     * @param userId ユーザーID
     */
    private void updatePurchases(final List<TPurchaseEntity> dbPurchases,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date systemDate,
            final BigInteger userId) {
        // LG送信指示済にデータセット
        purchaseComponent.sortForUpdate(dbPurchases);
        purchaseComponent.prepareSaveData(dbPurchases, wmsLinkingFileEntity, userId);

        // 仕入確定済のデータセット
        dbPurchases.forEach(dbPurchase -> {
            // 入荷確定数
            dbPurchase.setFixArrivalCount(dbPurchase.getArrivalCount());
            // 計上日
            dbPurchase.setRecordAt(systemDate);
        });

        // 仕入情報更新
        purchaseRepository.saveAll(dbPurchases);
    }

    /**
     * 仕入確定関連のデータ更新.
     * ・納品明細情報 t_delivery_detail
     * ・納品SKU t_delivery_sku
     * ・発注情報 t_order
     * ・発注SKU t_order_sku
     *
     * @param dbPurchases DB登録済の仕入情報リスト
     * @param systemDate システム日付
     * @param userId ユーザID
     */
    private void updatePurchaseConfirmData(
            final List<TPurchaseEntity> dbPurchases,
            final Date systemDate,
            final BigInteger userId) {
        // 取込用仕入情報Modelリスト作成
        final List<PurchaseImportModel> purchases = generatePurchaseImportModelList(dbPurchases);

        // 納品明細情報更新対象抽出
        final List<PurchaseImportModel> filterPurchaseImportsForDelivery = purchases.stream()
                .filter(purchaseConfirmComponent::isDeliveryUpdateTarget)
                .collect(Collectors.toList());

        // 納品依頼明細の更新
        updateDeliveryDetails(filterPurchaseImportsForDelivery, userId);

        // 発注情報更新対象抽出
        final List<PurchaseImportModel> filterPurchaseImportsForOrder = purchases.stream()
                .filter(purchaseConfirmComponent::isOrderUpdateTarget)
                .collect(Collectors.toList());

        // 発注情報の更新
        updateOrders(filterPurchaseImportsForOrder, systemDate, userId);

        // 納品SKUと発注SKUの更新
        updateDeliverySkuAndOrderSku(filterPurchaseImportsForOrder, systemDate, userId);
    }

    /**
     * 取込用仕入情報Modelリスト作成.
     *
     * @param dbPurchases DB登録済の仕入情報リスト
     * @return 取込用仕入情報リスト
     */
    private List<PurchaseImportModel> generatePurchaseImportModelList(final List<TPurchaseEntity> dbPurchases) {
        return dbPurchases.stream()
                .map(dbPurchase -> {
                    final PurchaseImportModel purchaseImportModel = new PurchaseImportModel();
                    BeanUtils.copyProperties(dbPurchase, purchaseImportModel);
                    return purchaseImportModel;
                }).collect(Collectors.toList());
    }

    /**
     * 納品依頼明細情報更新.
     *
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param updatedUserId 更新ユーザID
     */
    private void updateDeliveryDetails(
            final List<PurchaseImportModel> purchaseImportModels,
            final BigInteger updatedUserId) {
        // 取込用仕入情報Modelを課単位で重複除去してループ
        purchaseConfirmComponent.distinctPurchaseByDivision(purchaseImportModels)
        .forEach(purchaseImportModel -> {
            // 納品明細情報の更新
            final TDeliveryDetailEntity updateDeliveryDetail = purchaseConfirmComponent.generateUpdateDeliveryDetail(purchaseImportModel, updatedUserId);
            deliveryDetailRepository.save(updateDeliveryDetail);
        });
    }

    /**
     * 発注情報更新.
     *
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    private void updateOrders(
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        if (CollectionUtils.isEmpty(purchaseImportModels)) {
            return;
        }

        final List<TOrderEntity> updateOrders = purchaseConfirmComponent.generateUpdateOrders(
                purchaseImportModels, accountingDate, updatedUserId);

        orderRepository.saveAll(updateOrders);
    }

    /**
     * 納品SKU情報と発注SKU情報更新.
     *
     * @param purchaseImportModels 取込用の仕入情報Modelリスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    private void updateDeliverySkuAndOrderSku(
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        // 取込用仕入情報Modelを発注SKU単位で重複除去してループ
        purchaseConfirmComponent.distinctPurchaseByOrderSku(purchaseImportModels)
        .forEach(distinctByOrderSkuPurchase -> {
            final TOrderSkuEntity orderSkuEntity = purchaseConfirmComponent.findByOrderIdAndColorAndSize(distinctByOrderSkuPurchase);

            purchaseConfirmComponent.filterOrderSkuPurchaseByOrderSku(purchaseImportModels, distinctByOrderSkuPurchase)
            .forEach(orderSkuPurchase -> {
                final TDeliverySkuEntity deliverySkuEntity = purchaseConfirmComponent.findDeliverySkuEntity(orderSkuPurchase);
                // 納品依頼SKU更新
                purchaseConfirmComponent.generateUpdateDeliverySku(orderSkuPurchase, deliverySkuEntity, updatedUserId);
                deliverySkuRepository.save(deliverySkuEntity);
                // 発注SKU更新
                purchaseConfirmComponent.generateUpdateOrderSku(orderSkuPurchase, orderSkuEntity, deliverySkuEntity, accountingDate, updatedUserId);
                orderSkuRepository.save(orderSkuEntity);
            });
        });
    }

    /**
     * 配分出荷データの確定.
     * (1)倉庫連携ファイル情報登録(QH)
     * (2)配分出荷確定関連データ更新
     *
     * @param deliveryModel 納品依頼情報Model
     * @param systemDate システム日付
     * @param userId ユーザーID
     */
    private void confirmDistributionShipmentData(final DeliveryModel deliveryModel,
            final Date systemDate,
            final BigInteger userId) {
        // 倉庫連携ファイル情報登録(業務区分：QH)
        final TWmsLinkingFileEntity shipmentWms = distributionShipmentComponent
                .insertWmsLinkingFile(BusinessType.DIRECT_DISTRIBUTION_SHIPMENT_CONFIRM);

        // 配分出荷確定
        confirmDistributionShipments(deliveryModel, shipmentWms, systemDate, userId);
    }

    /**
     * 直送確定：配分出荷情報更新.
     * ・納品明細情報 t_delivery_detail
     * ・納品得意先SKU情報 t_delivery_store_sku
     * 配分出荷確定済に更新.
     *
     * @param deliveryModel 納品依頼情報Model
     * @param wmsLinkingFileEntity DB登録済の倉庫連携ファイル情報Entity
     * @param systemDate システム日付
     * @param userId ユーザーID
     */
    private void confirmDistributionShipments(final DeliveryModel deliveryModel,
            final TWmsLinkingFileEntity wmsLinkingFileEntity,
            final Date systemDate,
            final BigInteger userId) {
        // 更新対象の納品明細情報取得
        final List<BigInteger> deliveryDetailIds = deliveryModel.getDeliveryDetails().stream().map(entity -> entity.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliveryDetailIds)) {
            // 納品明細IDリストが空の場合はエラー
            throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }
        final List<TDeliveryDetailEntity> dbDeliveryDetails = deliveryDetailRepository.findByIds(deliveryDetailIds).get();

        // LG送信指示済にデータセット
        distributionShipmentComponent.sortForUpdate(dbDeliveryDetails);
        final List<TDeliveryStoreSkuEntity> deliveryStoreSkus = new ArrayList<TDeliveryStoreSkuEntity>();
        distributionShipmentComponent.prepareSaveData(dbDeliveryDetails, deliveryStoreSkus, wmsLinkingFileEntity, userId);

        // 配分出荷確定済にデータセット:
        final List<TDeliveryStoreEntity> deliveryStores = new ArrayList<>();
        // 納品明細
        dbDeliveryDetails.forEach(dbDeliveryDetail -> {
            // 配分出荷日
            dbDeliveryDetail.setAllocationCargoAt(systemDate);
            // 配分完了フラグ
            dbDeliveryDetail.setAllocationCompleteFlg(BooleanType.TRUE);
            // 配分完了日
            dbDeliveryDetail.setAllocationCompleteAt(systemDate);
            // 配分計上日
            dbDeliveryDetail.setAllocationRecordAt(systemDate);

            // 納品明細IDを基に納品得意先情報取得
            final Page<TDeliveryStoreEntity> stores = deliveryStoreRepository.findByDeliveryDetailId(
                    dbDeliveryDetail.getId(),
                    PageRequest.of(0, Integer.MAX_VALUE));
            if (!stores.hasContent()) {
                // 取得できない場合はエラー
                throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
            }
            stores.forEach(store -> deliveryStores.add(store));
        });

        // 納品得意先SKU
        distributionShipmentComponent.generateConfirmDeliveryStoreSku(deliveryStoreSkus, deliveryStores);

        // 納品依頼明細情報更新
        deliveryDetailRepository.saveAll(dbDeliveryDetails);
        // 得意先SKU情報更新
        deliveryStoreSkuRepository.saveAll(deliveryStoreSkus);
    }

    /**
     * 納品伝票ファイル情報登録.
     * 1納品依頼につき2レコード登録(配分出荷伝票とピッキングリスト用に1レコードずつ)
     *
     * @param deliveryModel 納品依頼情報
     */
    private void createDeliveryVoucherFileInfo(final DeliveryModel deliveryModel) {
        final List<TDeliveryVoucherFileInfoEntity> deliveryVoucherFileInfos = new ArrayList<>();

        // 配分出荷伝票
        deliveryVoucherFileInfos.add(
                generateDeliveryVoucherFileInfo(deliveryModel, DeliveryVoucherCategoryType.SHIPPING_DISTRIBUTION_VOUCHER));
        // ピッキングリスト
        deliveryVoucherFileInfos.add(generateDeliveryVoucherFileInfo(deliveryModel, DeliveryVoucherCategoryType.PICKING_LIST));

        deliveryVoucherFileInfoRepository.saveAll(deliveryVoucherFileInfos);
    }

    /**
     * 登録用納品伝票ファイル情報作成.
     *
     * @param deliveryModel 納品依頼情報
     * @param voucherCategory 伝票分類
     * @return 納品伝票ファイル情報
     */
    private TDeliveryVoucherFileInfoEntity generateDeliveryVoucherFileInfo(
            final DeliveryModel deliveryModel,
            final DeliveryVoucherCategoryType voucherCategory) {
        final TDeliveryVoucherFileInfoEntity entity = new TDeliveryVoucherFileInfoEntity();

        entity.setDeliveryId(deliveryModel.getId());
        entity.setDeliveryCount(deliveryModel.getDeliveryCount());
        entity.setOrderId(deliveryModel.getOrderId());
        entity.setVoucherCategory(voucherCategory);
        entity.setStatus(FileInfoStatusType.FILE_UNPROCESSED);

        return entity;
    }
}
