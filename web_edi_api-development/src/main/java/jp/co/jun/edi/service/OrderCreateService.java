package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.LoginUserComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.OrderRegistSendMailComponent;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.constants.DefaultValueConstants;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSkuModel;
import jp.co.jun.edi.model.mail.OrderRegistSendModel;
//PRD_0112 #7710 JFE add start
import jp.co.jun.edi.repository.MSirmstRepository;
//PRD_0112 #7710 JFE add end
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.ProductionType;
import jp.co.jun.edi.type.ReissueType;
import jp.co.jun.edi.util.NumberUtils;

/**
 * 発注、発注SKU作成処理.
 */
@Service
public class OrderCreateService extends GenericCreateService<CreateServiceParameter<OrderModel>, CreateServiceResponse<OrderModel>> {
    @Autowired
    private TOrderRepository orderRepository;

    // PRD_0112 #7710 JFE add start
    @Autowired
    private MSirmstRepository sirmstrepository;
    // PRD_0112 #7710 JFE add end

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    @Autowired
    private OrderRegistSendMailComponent orderRegistSendMailComponent;

//  PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
//  PRD_0142 #10423 JFE add end

    @Override
    protected CreateServiceResponse<OrderModel> execute(final CreateServiceParameter<OrderModel> serviceParameter) {
        final OrderModel orderModel = serviceParameter.getItem();

        // 品番情報取得
        final ExtendedTItemEntity extendedTItem = itemComponent.getExtendedTItem(orderModel.getPartNoId());
        // バリデーションチェック
        // PRD_0112 #7710 JFE mod start
//        ResultMessages rsltMsg = checkValidate(extendedTItem);
//        if (rsltMsg.isNotEmpty()) {
//            throw new BusinessException(rsltMsg);
//        }
        ResultMessages rsltMsg = checkValidate(extendedTItem,orderModel);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
        // PRD_0112 #7710 JFE mod end

        // 発注情報をEntityにコピー
        final TOrderEntity tOrder = new TOrderEntity();
        // 発注情報の値セット
        setValueForOrderEntity(serviceParameter.getLoginUser(), orderModel, tOrder);

        // 発注情報の登録
        orderRepository.save(tOrder);

        // 発注SKUの登録
        if (Objects.nonNull(orderModel.getOrderSkus()) && !orderModel.getOrderSkus().isEmpty()) {
            final List<TOrderSkuEntity> tOrderSkus = orderModel.getOrderSkus().stream().map(
                    orderSku -> {
                        final TOrderSkuEntity tOrderSku = new TOrderSkuEntity();
                        // 発注SKU情報の値セット
                        setValueForOrderSkuEntity(tOrder.getId(), orderSku, tOrderSku);
                        return tOrderSku;
                    }).collect(Collectors.toList());

            orderSkuRepository.saveAll(tOrderSkus);
        }

        // レスポンスに発注ID、発注Noを設定
        orderModel.setId(tOrder.getId());
        orderModel.setOrderNumber(tOrder.getOrderNumber());

         // メール送信情報用データ取得。
        final ExtendedTOrderEntity extendedTOrder = extendedTOrderRepository.findById(tOrder.getId()).orElse(new ExtendedTOrderEntity());

        // メール送信
        OrderRegistSendModel sendModel = generateSendMailData(extendedTItem, extendedTOrder);
        orderRegistSendMailComponent.sendMail(sendModel, serviceParameter.getLoginUser().getAccountName());

        return CreateServiceResponse.<OrderModel>builder().item(orderModel).build();
    }

    /**
     * 入力された発注情報を登録用の発注情報に詰め替え.
     *
     * @param loginUser ログインユーザ情報
     * @param orderModel 画面から入力された発注情報
     * @param orderEntity 登録用の発注情報
     *
     * @return 画面の情報とAPI側で算出する情報をセットした発注情報Entity
     */
    private TOrderEntity setValueForOrderEntity(final CustomLoginUser loginUser, final OrderModel orderModel, final TOrderEntity orderEntity) {
        /** 画面側で設定される項目 */
        // 品番ID
        orderEntity.setPartNoId(orderModel.getPartNoId());
        // 品番
        orderEntity.setPartNo(orderModel.getPartNo());
        // 費目
        orderEntity.setExpenseItem(orderModel.getExpenseItem());
        // 裁断自動区分
        orderEntity.setCutAutoType(orderModel.getCutAutoType());
        // 製品発注日
        orderEntity.setProductOrderAt(orderModel.getProductOrderAt());
        // 製品修正納期
        orderEntity.setProductCorrectionDeliveryAt(orderModel.getProductCorrectionDeliveryAt());
        // 生産メーカーコード
        orderEntity.setMdfMakerCode(orderModel.getMdfMakerCode());
        // 生産工場コード
        orderEntity.setMdfMakerFactoryCode(orderModel.getMdfMakerFactoryCode());
        // 生産工場名
        orderEntity.setMdfMakerFactoryName(orderModel.getMdfMakerFactoryName());
        // 製造担当
        orderEntity.setMdfStaffCode(orderModel.getMdfStaffCode());
        // 委託先工場
        orderEntity.setConsignmentFactory(orderModel.getConsignmentFactory());
        // 原産国
        orderEntity.setCooCode(orderModel.getCooCode());
        // 上代
        orderEntity.setRetailPrice(orderModel.getRetailPrice());
        // 単価
        orderEntity.setUnitPrice(orderModel.getUnitPrice());
        // その他原価
        orderEntity.setOtherCost(orderModel.getOtherCost());
        // 摘要
        orderEntity.setApplication(orderModel.getApplication());
        // PRD_0144 #10776 add JFE start
        // 関連No
        if (orderEntity.getExpenseItem() == ExpenseItemType.SEWING_ORDER && orderModel.getRelationNumber() == null) {
        	// 費目04(縫製発注)でnullの場合、0をセット
        	orderEntity.setRelationNumber(BigInteger.ZERO);
        } else {
        	orderEntity.setRelationNumber(orderModel.getRelationNumber());
        }
        // PRD_0144 #10776 add JFE end

        /** APIで計算する項目 */
        // 生産区分をセット
        orderEntity.setProductionType(ProductionType.DEFAULT);
        // 素材をセット
        orderEntity.setMaterial(DefaultValueConstants.DEFAULT_MATERIAL);
        // 生地メーカーをセット
        orderEntity.setMatlMakerCode(DefaultValueConstants.DEFAULT_MATL_MAKER_CODE);
        // 生地仕入区分をセット
        orderEntity.setMatlPurchaseType(DefaultValueConstants.DEFAULT_STRING_ZERO);
        // 生地m数をセット
        orderEntity.setMatlMeter(BigDecimal.ZERO);
//      PRD_0206 && TEAM_ALBUS-16 mod start
        // 生地単価をセット
        //orderEntity.setMatlUnitPrice(BigDecimal.ZERO);
        if(orderModel.getMatlUnitPrice() != null) {
            orderEntity.setMatlUnitPrice(orderModel.getMatlUnitPrice());
        }else {
            orderEntity.setMatlUnitPrice(BigDecimal.ZERO);
        }
//      PRD_0206 && TEAM_ALBUS-16 mod end

        // 反数をセット
        orderEntity.setClothCount(BigDecimal.ZERO);
        // 予定用尺をセット
        orderEntity.setPlanLengthActual(orderComponent.setValuePlanLengthActualAndNecessaryLengthActual(orderModel.getExpenseItem()));
        // 実用尺をセット
        orderEntity.setNecessaryLengthActual(
                orderComponent.setValuePlanLengthActualAndNecessaryLengthActual(orderModel.getExpenseItem()));
        // 生地原価をセット
//      PRD_0023 && No_65 mod JFE start
        orderEntity.setMatlCost(orderModel.getMatlCost());
//      PRD_0023 && No_65 mod JFE end
        // 製品納期をセット(=製品修正納期)
        orderEntity.setProductDeliveryAt(orderModel.getProductCorrectionDeliveryAt());
        // 再発行フラグをセット
        orderEntity.setReissueFlg(ReissueType.NO_REISSUE);
        // 反巾をセット
        orderEntity.setClothWidth(BigInteger.ZERO.intValue());
        // 反長をセット
        orderEntity.setClothLength(BigInteger.ZERO.intValue());
        // 数量(SKUの合計数から算出)
        orderEntity.setQuantity(BigDecimal.valueOf(orderComponent.sumProductOrderLot(orderModel.getOrderSkus())));
        // 製品金額をセット
        orderEntity.setProductPrice(orderModel.getUnitPrice().multiply(orderEntity.getQuantity()));
//      PRD_0206 && TEAM_ALBUS-16 add start
        // 製品原価(生地原価 + 加工原価 + 附属原価 + その他原価をセット)
        // nullの場合は0へ変換
        //orderEntity.setProductCost(orderModel.getOtherCost());
        BigDecimal matlCost = NumberUtils.defaultInt(orderModel.getMatlCost());
        BigDecimal processingCost = NumberUtils.defaultInt(orderModel.getProcessingCost());
        BigDecimal attachedCost = NumberUtils.defaultInt(orderModel.getAttachedCost());
        BigDecimal otherCost = NumberUtils.defaultInt(orderModel.getOtherCost());;
        orderEntity.setProductCost(matlCost.add(processingCost).add(attachedCost).add(otherCost));
//      PRD_0206 && TEAM_ALBUS-16 add end
        // 加工賃をセット
//      PRD_0023 && No_65 mod JFE start
        orderEntity.setProcessingCost(orderModel.getProcessingCost());
        // 付属代をセット
        orderEntity.setAttachedCost(orderModel.getAttachedCost());
//      PRD_0023 && No_65 mod JFE start
        // 送信区分をセット
        orderEntity.setSendCode(DefaultValueConstants.DEFAULT_SEND_CODE);
        // 社内の場合連携入力者をセット
        orderEntity.setJunpcTanto(loginUserComponent.getAccountNameWithAffiliation(loginUser));
//      PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        orderEntity.setTagdatCreatedFlg(NOT_CREATED);
//      PRD_0142 #10423 JFE add end

        // 連携対象に更新
        orderComponent.setLinkingTarget(orderEntity);

        return orderEntity;
    }

    /**
     * 入力された発注SKU情報を登録用の発注情報に詰め替え.
     *
     * @param orderId 発注ID
     * @param orderSkuModel 画面から入力された発注SKU情報
     * @param orderSkuEntity 登録用の発注Sku情報
     *
     * @return 画面の情報とAPI側で算出する情報をセットした発注Sku情報Entity
     */
    private TOrderSkuEntity setValueForOrderSkuEntity(final BigInteger orderId, final OrderSkuModel orderSkuModel, final TOrderSkuEntity orderSkuEntity) {
        /** 画面側で設定される項目 */
        // 品番
        orderSkuEntity.setPartNo(orderSkuModel.getPartNo());
        // 色
        orderSkuEntity.setColorCode(orderSkuModel.getColorCode());
        // サイズ
        orderSkuEntity.setSize(orderSkuModel.getSize());
        // 製品発注数
        orderSkuEntity.setProductOrderLot(orderSkuModel.getProductOrderLot());

        /** APIで計算する項目 */
        // 発注ID
        orderSkuEntity.setOrderId(orderId);
        // 発注No
        orderSkuEntity.setOrderNumber(BigInteger.ZERO);
        // 月末発注数量
        orderSkuEntity.setMonthEndOrderLot(BigInteger.ZERO.intValue());          // 当月
        orderSkuEntity.setPreviousMonthEndOrderLot(BigInteger.ZERO.intValue());  // 前月
        orderSkuEntity.setMonthBeforeEndOrderLot(BigInteger.ZERO.intValue());    // 前々月
        // 月末裁断数量
        orderSkuEntity.setMonthEndCutLot(BigInteger.ZERO.intValue());          // 当月
        orderSkuEntity.setPreviousMonthEndCutLot(BigInteger.ZERO.intValue());  // 前月
        orderSkuEntity.setMonthBeforeEndCutLot(BigInteger.ZERO.intValue());    // 前々月
        // 送信区分
        orderSkuEntity.setSendCode(DefaultValueConstants.DEFAULT_SEND_CODE);

        return orderSkuEntity;
    }

    /**
     * メール送信用データ作成.
     * @param extendedTItemEntity ExtendedTItemEntity
     * @param extendedTOrderEntity ExtendedTOrderEntity
     * @return OrderRegistSendModel
     */
    private OrderRegistSendModel generateSendMailData(final ExtendedTItemEntity extendedTItemEntity, final ExtendedTOrderEntity extendedTOrderEntity) {

        OrderRegistSendModel orderRegistSendModel = new OrderRegistSendModel();
        BeanUtils.copyProperties(extendedTItemEntity, orderRegistSendModel);

        orderRegistSendModel.setMdfMakerCode(extendedTOrderEntity.getMdfMakerCode());
        orderRegistSendModel.setMdfMakerName(extendedTOrderEntity.getMdfMakerName());
        orderRegistSendModel.setMdfStaffCode(extendedTOrderEntity.getMdfStaffCode());
        orderRegistSendModel.setQuantity(extendedTOrderEntity.getQuantity());
        // ※納期は製品修正納期をセット!
        orderRegistSendModel.setProductDeliveryAt(extendedTOrderEntity.getProductCorrectionDeliveryAt());

        return orderRegistSendModel;
    }

    /**
     * バリデーションチェックを行う.
     * @param extendedTItemEntity 品番情報
     * @param orderModel 受注情報
     * @return ResultMessages
     */
//    private ResultMessages checkValidate(final ExtendedTItemEntity extendedTItemEntity) {
//        // 外部連携区分:JUNoT登録以外の場合、登録不可
//        itemComponent.validateReadOnly(extendedTItemEntity.getExternalLinkingType());
//
//        final ResultMessages rsltMsg = ResultMessages.warning();
//
//        // 品番が消化委託の場合は登録不可
//        if (extendedTItemEntity.isDigestionCommissionType()) {
//            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_O_006));
//        }
//
//        return rsltMsg;
//    }
    private ResultMessages checkValidate(final ExtendedTItemEntity extendedTItemEntity,OrderModel orderModel) {
        // 外部連携区分:JUNoT登録以外の場合、登録不可
        itemComponent.validateReadOnly(extendedTItemEntity.getExternalLinkingType());

        final ResultMessages rsltMsg = ResultMessages.warning();

        // 品番が消化委託の場合は登録不可
        if (extendedTItemEntity.isDigestionCommissionType()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_O_006));
        }
        // PRD_0112 #7710 JFE add start
        // 発注先メーカー情報を取得できなければエラーを返す
        final Optional<MSirmstEntity> orderMaker =  sirmstrepository.findByOrderMakerId(orderModel.getMdfMakerCode());
        if (orderMaker.isPresent() == false){
        	rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_I_32));
        }
        // PRD_0112 #7710 JFE add end

        return rsltMsg;
    }
}
