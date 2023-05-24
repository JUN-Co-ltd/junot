package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.PurchaseImportModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.repository.MTnpmstRepository;
import jp.co.jun.edi.repository.TDeliveryDetailRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.type.SendType;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * 仕入確定(直送確定)関連のコンポーネント.
 */
@Component
public class PurchaseConfirmComponent extends GenericComponent {
    /** 指示番号. */
    private static final String INSTRUCT_NUMBER = "000000";

    @Autowired
    private ShipmentComponent shipmentComponent;

    @Autowired
    private MTnpmstRepository tnpmstRepository;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private TDeliveryDetailRepository deliveryDetailRepository;

    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    /**
     * 直送確定用に登録する仕入情報を作成.
     *
     * @param deliveryModel 納品依頼情報Model
     * @param orderEntity 発注情報
     * @param systemDate システム日付
     * @return PurchaseModel
     */
    public List<TPurchaseEntity> generateDirectConfirmPurchases(
            final DeliveryModel deliveryModel,
            final TOrderEntity orderEntity,
            final Date systemDate) {
        final List<TPurchaseEntity> directConfirmPurchases = deliveryModel.getDeliveryDetails()
                .stream()
                .flatMap(deliveryDetail -> deliveryDetail.getDeliverySkus()
                        .stream()
                        .map(deliverySku -> generatePurchaseEntity(deliveryModel, deliveryDetail, deliverySku,
                                orderEntity, systemDate)))
                .collect(Collectors.toList());

        return directConfirmPurchases;
    }

    /**
     * 登録用仕入情報の作成.
     *
     * @param deliveryModel 納品依頼
     * @param deliveryDetailModel 納品依頼明細
     * @param deliverySkuModel 納品依頼SKU
     * @param orderEntity 発注情報
     * @param systemDate システム日付
     * @return 仕入情報
     */
    private TPurchaseEntity generatePurchaseEntity(
            final DeliveryModel deliveryModel,
            final DeliveryDetailModel deliveryDetailModel,
            final DeliverySkuModel deliverySkuModel,
            final TOrderEntity orderEntity,
            final Date systemDate) {
        final TPurchaseEntity entity = new TPurchaseEntity();
        // 店舗コード
        final String shpcd = tnpmstRepository.findShopCodeForDirectDelivery();

        // データ種別
        entity.setDataType(PurchaseDataType.SR);
        // 仕入区分
        entity.setPurchaseType(PurchaseType.DIRECT_PURCHASE);
        // 入荷場所
        entity.setArrivalPlace(shipmentComponent.extraxtOldLogisticsCode(shpcd));
        // 入荷店舗
        entity.setArrivalShop(shpcd);
        // 仕入先
        entity.setSupplierCode(orderEntity.getMdfMakerCode());
        // 製品工場
        entity.setMdfMakerFactoryCode(orderEntity.getMdfMakerFactoryCode());
        // 入荷日
        entity.setArrivalAt(systemDate);
        // 品番ID
        entity.setPartNoId(orderEntity.getPartNoId());
        // 品番
        entity.setPartNo(orderEntity.getPartNo());
        // 色
        entity.setColorCode(deliverySkuModel.getColorCode());
        // サイズ
        entity.setSize(deliverySkuModel.getSize());
        // 入荷数
        entity.setArrivalCount(deliverySkuModel.getDeliveryLot());
        // 良品・不用品区分
        entity.setNonConformingProductType(BooleanType.FALSE);
        // 指示番号
        entity.setInstructNumber(INSTRUCT_NUMBER);
        // 指示番号行
        entity.setInstructNumberLine(0);
        // 発注ID
        entity.setOrderId(deliveryModel.getOrderId());
        // 発注番号
        entity.setOrderNumber(deliveryModel.getOrderNumber());
        // 引取回数
        entity.setPurchaseCount(deliveryModel.getDeliveryCount());
        // 課コード
        entity.setDivisionCode(deliveryDetailModel.getDivisionCode());
        // 仕入単価(B級品の場合はB級品単価。それ以外は発注情報の単価)
        if (deliveryModel.isNonConformingProductType()) {
            entity.setPurchaseUnitPrice(deliveryModel.getNonConformingProductUnitPrice().intValue());
        } else {
            entity.setPurchaseUnitPrice(orderEntity.getUnitPrice().intValue());
        }
        // 納品ID
        entity.setDeliveryId(deliveryModel.getId());
        // LG送信区分　0:LG送信未指示
        entity.setLgSendType(LgSendType.NO_INSTRUCTION);
        // 会計連携ステータス　0:ファイル未処理
        entity.setAccountLinkingStatus(FileInfoStatusType.FILE_UNPROCESSED);

        return entity;

    }

    /**
     * 納品ID、課コード、引取回数をキーに重複除去.
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @return 課単位で重複除去した取込用仕入情報リスト
     */
    public List<PurchaseImportModel> distinctPurchaseByDivision(final List<PurchaseImportModel> purchaseImportModels) {
        return purchaseImportModels.stream()
                .filter(jp.co.jun.edi.util.CollectionUtils.distinctByKey(model -> model.getDeliveryId() + model.getDivisionCode() + model.getPurchaseCount()))
                .collect(Collectors.toList());
    }

    /**
     * 仕入確定更新用納品依頼明細情報作成.
     * @param purchaseImportModel 取込用の仕入情報
     * @param updatedUserId 更新ユーザID
     * @return 納品依頼明細情報
     */
    public TDeliveryDetailEntity generateUpdateDeliveryDetail(
            final PurchaseImportModel purchaseImportModel,
            final BigInteger updatedUserId) {
        final TDeliveryDetailEntity deliveryDetailEntity = findDeliveryDetailByDivisionCodeAndDeliveryIdAndCount(purchaseImportModel);

        deliveryDetailEntity.setArrivalNumber(purchaseImportModel.getSqManageNumber());
        deliveryDetailEntity.setArrivalPlace(purchaseImportModel.getArrivalPlace());
        deliveryDetailEntity.setAllocationConfirmFlg(BooleanType.TRUE);
        deliveryDetailEntity.setArrivalAt(purchaseImportModel.getArrivalAt());
        deliveryDetailEntity.setArrivalFlg(BooleanType.TRUE);
        deliveryDetailEntity.setPickingFlg(BooleanType.TRUE);
        deliveryDetailEntity.setAllocationCompleteFlg(BooleanType.TRUE);
        deliveryDetailEntity.setSendCount(0);
        deliveryDetailEntity.setUpdatedUserId(updatedUserId);

        return deliveryDetailEntity;
    }

    /**
     * @param purchaseImportModel 取込用の仕入情報
     * @return 納品明細情報
     */
    private TDeliveryDetailEntity findDeliveryDetailByDivisionCodeAndDeliveryIdAndCount(final PurchaseImportModel purchaseImportModel) {
        return deliveryDetailRepository.findByDivisionCodeAndDeliveryIdAndCount(
                purchaseImportModel.getDeliveryId(),
                purchaseImportModel.getDivisionCode(),
                purchaseImportModel.getPurchaseCount().intValue())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getDeliveryDetailInfo")
                        .message("t_delivery_detail not found.")
                        .build())));
    }

    /**
     * 発注ID、色コード、サイズをキーに重複除去.
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @return 発注SKU単位で重複除去した取込用仕入情報リスト
     */
    public List<PurchaseImportModel> distinctPurchaseByOrderSku(final List<PurchaseImportModel> purchaseImportModels) {
        return purchaseImportModels.stream()
                .filter(modle -> Objects.nonNull(modle.getOrderId()))
                .filter(jp.co.jun.edi.util.CollectionUtils.distinctByKey(model -> model.getOrderId() + model.getColorCode() + model.getSize()))
                .collect(Collectors.toList());
    }

    /**
     * 下記のケースのみ更新対象.
     * 発注IDが設定されている
     * AND
     * 「仕入区分」＝"1"（追加仕入(ﾘｶﾊﾞﾘ用)）
     * OR 「仕入区分」＝"6"（配分出荷）
     * OR 「仕入区分」＝"7"（直送仕入）
     * @param p 取込用仕入情報
     * @return true:納品明細情報、納品SKU情報の更新対象
     */
    public boolean isDeliveryUpdateTarget(final PurchaseImportModel p) {

        if (Objects.isNull(p.getOrderId())) {
            return false;
        }

        switch (p.getPurchaseType()) {
            case ADDITIONAL_PURCHASE:
            case SHIPMENT_PURCHASE_DIVISION:
            case DIRECT_PURCHASE:
                return true;
            default:
                return false;
        }
    }

    /**
     * 下記のケースのみ更新対象.
     * 発注IDが設定されている
     * AND
     * 「仕入区分」＝"1"（追加仕入(ﾘｶﾊﾞﾘ用)）
     *  OR （「仕入区分」＝"3"（仕入返品） AND 「入荷場所」≠"19"（消化委託））
     *  OR 「仕入区分」＝"6"（配分出荷）
     *  OR 「仕入区分」＝"7"（直送仕入）
     *  OR （「仕入区分」＝"9"（店舗仕入） AND 「入荷場所」≠"19"（消化委託））
     * @param p 取込用仕入情報
     * @return true:発注情報、発注SKU情報の更新対象
     */
    public boolean isOrderUpdateTarget(final PurchaseImportModel p) {

        if (Objects.isNull(p.getOrderId())) {
            return false;
        }

        switch (p.getPurchaseType()) {
            case ADDITIONAL_PURCHASE:
            case SHIPMENT_PURCHASE_DIVISION:
            case DIRECT_PURCHASE:
                return true;
            case RETURN_PURCHASE:
            case STORE_PURCHASE:
                return !("19".equals(p.getArrivalPlace()));
            default:
                return false;
        }
    }

    /**
     * 仕入確定更新用発注情報リスト作成.
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     * @return 発注情報リスト
     */
    public List<TOrderEntity> generateUpdateOrders(
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId) {
        final List<TOrderEntity> updateOrders = new ArrayList<>();

        purchaseImportModels.stream()
        .map(PurchaseImportModel::getOrderId)
        .distinct()
        .forEach(orderId -> {
            final TOrderEntity orderEntity = generateUpdateOrderEntity(orderId, purchaseImportModels, accountingDate, updatedUserId);
            updateOrders.add(orderEntity);
            });

        return updateOrders;
    }

    /**
     * 仕入確定更新用発注情報作成.
     * @param orderId 発注情報
     * @param purchaseImportModels 取込用の仕入情報リスト
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     * @return 発注情報
     */
    private TOrderEntity generateUpdateOrderEntity(
            final BigInteger orderId,
            final List<PurchaseImportModel> purchaseImportModels,
            final Date accountingDate,
            final BigInteger updatedUserId)  {
        final TOrderEntity orderEntity = findOrderByOrderId(orderId);
        // 発注IDに紐づく仕入確定データを抽出
        final List<PurchaseImportModel> orderPurchase = purchaseImportModels
                .stream()
                .filter(p -> p.getOrderId() == orderId)
                .collect(Collectors.toList());

        // 製品最終処理日：仕入返品以外が存在する場合のみ設定する.
        if (orderPurchase.stream().anyMatch(p -> PurchaseType.RETURN_PURCHASE != p.getPurchaseType())) {
          orderEntity.setProductLastDisposalAt(accountingDate);

          // 最新製品仕入日：日付が新しい場合のみ設定する
          final Date currentProductPurchaseAt = orderEntity.getCurrentProductPurchaseAt();
          if (currentProductPurchaseAt == null || accountingDate.after(currentProductPurchaseAt)) {
              orderEntity.setCurrentProductPurchaseAt(accountingDate);
          }
        }

        // 最新製品返品日：仕入返品が存在し、日付が新しい場合のみ設定する.
        final Date currentProductReturnAt = orderEntity.getCurrentProductReturnAt();
        if (orderPurchase.stream().anyMatch(p -> PurchaseType.RETURN_PURCHASE == p.getPurchaseType())
                && (currentProductReturnAt == null || accountingDate.after(currentProductReturnAt))) {
            orderEntity.setCurrentProductReturnAt(accountingDate);
        }

        orderEntity.setProductCompleteType(CompleteType.COMPLETE);
        orderEntity.setSqSendType(SendType.SEND_TARGET);
        orderEntity.setUpdatedUserId(updatedUserId);

        return orderEntity;
    }

    /**
     * @param orderId 発注ID
     * @return 発注情報
     */
    private TOrderEntity findOrderByOrderId(final BigInteger orderId) {
        return orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getOrderInfo")
                        .message("t_order not found.")
                        .build())));
    }


    /**
     * @param distinctByOrderSkuPurchase 取込用仕入情報
     * @return 発注SKU情報
     */
    public TOrderSkuEntity findByOrderIdAndColorAndSize(final PurchaseImportModel distinctByOrderSkuPurchase) {

        return orderSkuRepository.findByOrderIdAndColorAndSize(
                distinctByOrderSkuPurchase.getOrderId(),
                distinctByOrderSkuPurchase.getColorCode(),
                distinctByOrderSkuPurchase.getSize())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getOrderSkuInfo")
                        .message("t_order_sku not found.")
                        .build())));
    }

    /**
     * @param purchaseImportModels 取込用の仕入情報Modelリスト
     * @param distinctByOrderSkuPurchase 取込用仕入情報
     * @return 発注SKU別の仕入確定リスト
     */
    public List<PurchaseImportModel> filterOrderSkuPurchaseByOrderSku(
            final List<PurchaseImportModel> purchaseImportModels,
            final PurchaseImportModel distinctByOrderSkuPurchase) {
        return purchaseImportModels.stream()
                .filter(p -> Objects.equals(p.getOrderId(), distinctByOrderSkuPurchase.getOrderId())
                        && Objects.equals(p.getColorCode(), distinctByOrderSkuPurchase.getColorCode())
                        && Objects.equals(p.getSize(), distinctByOrderSkuPurchase.getSize()))
                .collect(Collectors.toList());
    }

    /**
     * @param orderSkuPurchase 発注SKU別の仕入確定情報
     * @return 納品SKU情報
     */
    public TDeliverySkuEntity findDeliverySkuEntity(final PurchaseImportModel orderSkuPurchase) {
        return deliverySkuRepository.findByPurchaseInfo(
                orderSkuPurchase.getDeliveryId(),
                orderSkuPurchase.getPurchaseCount(),
                orderSkuPurchase.getDivisionCode(),
                orderSkuPurchase.getColorCode(),
                orderSkuPurchase.getSize())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("getDeliverySkuInfo")
                        .message("t_delivery_sku not found.")
                        .build())));
    }

    /**
     * 仕入確定更新用納品依頼SKU情報作成.
     * 仕入区分が3以外の場合に以下項目更新
     * ・入荷数量
     * ・更新ユーザーID
     * ※納品SKU更新対象外の場合は処理しない
     * ※仕入区分が3の場合は入荷数量再セットしない
     *
     * @param purchaseImportModel 取込用の仕入情報Model
     * @param deliverySkuEntity 納品依頼SKU情報
     * @param updatedUserId 更新ユーザID
     */
    public void generateUpdateDeliverySku(
            final PurchaseImportModel purchaseImportModel,
            final TDeliverySkuEntity deliverySkuEntity,
            final BigInteger updatedUserId) {
        if (!isDeliveryUpdateTarget(purchaseImportModel)) {
            // 納品SKU更新対象外の場合処理しない
            return;
        }
        // 仕入区分が3以外の場合、入荷数量再セット
        if (purchaseImportModel.getPurchaseType() != PurchaseType.RETURN_PURCHASE) {
            final int arrivalLot =
                    deliverySkuEntity.getArrivalLot() + purchaseImportModel.getFixArrivalCount().intValue();
            deliverySkuEntity.setArrivalLot(arrivalLot);
        }
        deliverySkuEntity.setUpdatedUserId(updatedUserId);
    }

    /**
     * 仕入確定更新用発注SKU情報作成.
     *
     * @param purchaseImportModel 取込用の仕入情報
     * @param orderSkuEntity 発注SKU情報
     * @param deliverySkuEntity 納品SKU情報
     * @param accountingDate 計上日
     * @param updatedUserId 更新ユーザID
     */
    public void generateUpdateOrderSku(
            final PurchaseImportModel purchaseImportModel,
            final TOrderSkuEntity orderSkuEntity,
            final TDeliverySkuEntity deliverySkuEntity,
            final Date accountingDate,
            final BigInteger updatedUserId) {

        // 納品依頼数量
        final int dsDeliveryLot = deliverySkuEntity.getDeliveryLot();
        final int deliveryLot = calculateMDeliveryLot(purchaseImportModel, orderSkuEntity.getDeliveryLot(), dsDeliveryLot);
        // 入荷数量
        final int arrivalLot = calculateMArrivalLot(purchaseImportModel, orderSkuEntity.getArrivalLot());
        // 仕入数
        final int purchaseLot = calculateMPurchaseLot(purchaseImportModel, orderSkuEntity.getPurchaseLot());
        // 返品数量
        final int returnLot = calculateMReturnLot(purchaseImportModel, orderSkuEntity.getReturnLot());
        // 純仕入数
        final int netPurchaseLot = calculateMNetPurchaseLot(purchaseImportModel, orderSkuEntity.getNetPurchaseLot());
        // 純仕入入金額
        final int netPurchaseDepositAmount = calculateMNetPurchaseDepositAmount(purchaseImportModel, orderSkuEntity.getNetPurchaseDepositAmount());

        orderSkuEntity.setDeliveryLot(deliveryLot);
        orderSkuEntity.setArrivalLot(arrivalLot);
        orderSkuEntity.setPurchaseLot(purchaseLot);
        orderSkuEntity.setReturnLot(returnLot);
        orderSkuEntity.setNetPurchaseLot(netPurchaseLot);
        orderSkuEntity.setNetPurchaseDepositAmount(netPurchaseDepositAmount);
        orderSkuEntity.setUpdatedUserId(updatedUserId);
    }

    /**
     *  (1) 「仕入確定データ.仕入区分」＝"3"（仕入返品）の場合、
     *      「仕入確定データ.入荷確定数」で減算する.
     *  (2) 「仕入確定データ.仕入区分」＝"1"（追加仕入(ﾘｶﾊﾞﾘ用)）
     *        OR 「仕入確定データ.仕入区分」＝"6"（配分出荷）
     *        OR 「仕入確定データ.仕入区分」＝"7"（直送仕入）の場合、
     *      「発注番号、納品依頼回数、課、カラー、サイズ」が一致する納品SKUテーブルの
     *      「納品数量」の合計値で減算し、
     *      「仕入確定データ.入荷確定数」で加算する.
     *  (3) 上記以外の場合、
     *      「仕入確定データ.入荷確定数」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param osDeliveryLot 発注SKU情報の納品依頼数量
     * @param dsDeliveryLot 納品SKU情報の納品数量
     * @return 納品依頼数
     */
    private int calculateMDeliveryLot(
            final PurchaseImportModel purchaseImportModel,
            final int osDeliveryLot,
            final int dsDeliveryLot) {
        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return osDeliveryLot - purchaseImportModel.getFixArrivalCount();
            case ADDITIONAL_PURCHASE:
            case SHIPMENT_PURCHASE_DIVISION:
            case DIRECT_PURCHASE:
                return osDeliveryLot
                        - dsDeliveryLot
                        + purchaseImportModel.getFixArrivalCount();
            default:
                return osDeliveryLot + purchaseImportModel.getFixArrivalCount();
        }
    }

    /**
     * (1) 「仕入確定データ.仕入区分」＝"3"（仕入返品）の場合、
     *     「仕入確定データ.入荷確定数」で減算する.
     * (2) 上記以外の場合、
     *     「仕入確定データ.入荷確定数」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param arrivalLot 発注SKU情報の入荷数量
     * @return 入荷数量
     */
    private int calculateMArrivalLot(
            final PurchaseImportModel purchaseImportModel,
            final int arrivalLot) {
        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return arrivalLot - purchaseImportModel.getFixArrivalCount();
            default:
                return arrivalLot + purchaseImportModel.getFixArrivalCount();
        }
    }

    /**
     * (1) 「仕入確定データ.仕入区分」≠"3"（仕入返品）の場合、
     *     「仕入確定データ.入荷確定数」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param purchaseLot 発注SKU情報の仕入数
     * @return 仕入数
     */
    private int calculateMPurchaseLot(
            final PurchaseImportModel purchaseImportModel,
            final int purchaseLot) {
        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return purchaseLot;
            default:
                return purchaseLot + purchaseImportModel.getFixArrivalCount();
        }
    }

    /**
     * (1) 「仕入確定データ.仕入区分」＝"3"（仕入返品）の場合、
     *     「仕入確定データ.入荷確定数」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param returnLot 発注SKU情報の返品数量
     * @return 返品数量
     */
    private int calculateMReturnLot(
            final PurchaseImportModel purchaseImportModel,
            final int returnLot) {
        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return returnLot + purchaseImportModel.getFixArrivalCount();
            default:
                return returnLot;
        }
    }

    /**
     * (1) 「仕入確定データ.仕入区分」＝"3"（仕入返品）の場合、
     *     「仕入確定データ.入荷確定数」で減算する.
     * (2) 上記以外の場合、
     *     「仕入確定データ.入荷確定数」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param netPurchaseLot 発注SKU情報の純仕入数
     * @return 純仕入数
     */
    private int calculateMNetPurchaseLot(
            final PurchaseImportModel purchaseImportModel,
            final int netPurchaseLot) {
        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return netPurchaseLot - purchaseImportModel.getFixArrivalCount();
            default:
                return netPurchaseLot + purchaseImportModel.getFixArrivalCount();
        }
    }

    /**
     *  (1) 「仕入確定データ.仕入区分」＝"3"（仕入返品）の場合、
     *      「仕入確定データ.入荷確定数」×「仕入確定データ.仕入単価」で減算する.
     *  (2) 上記以外の場合、
     *      「仕入確定データ.入荷確定数」×「仕入確定データ.仕入単価」で加算する.
     * @param purchaseImportModel 取込用の仕入情報
     * @param netPurchaseDepositAmount 発注SKU情報の純仕入金額
     * @return 純仕入金額
     */
    private int calculateMNetPurchaseDepositAmount(
            final PurchaseImportModel purchaseImportModel,
            final int netPurchaseDepositAmount) {
        final int val = purchaseImportModel.getFixArrivalCount() * purchaseImportModel.getPurchaseUnitPrice();

        switch (purchaseImportModel.getPurchaseType()) {
            case RETURN_PURCHASE:
                return netPurchaseDepositAmount - val;
            default:
                return netPurchaseDepositAmount + val;
        }
    }

}
