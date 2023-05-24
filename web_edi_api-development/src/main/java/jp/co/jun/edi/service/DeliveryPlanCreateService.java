package jp.co.jun.edi.service;

//PRD_0181 add JFE start
import java.math.BigDecimal;
//PRD_0181 add JFE end
import java.math.BigInteger;
//PRD_0181 add JFE start
import java.text.DecimalFormat;
//PRD_0181 add JFE end
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryPlanCreateMailDataComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.DeliveryPlanRegistSendMailComponent;
import jp.co.jun.edi.entity.HDeliveryPlanCutEntity;
import jp.co.jun.edi.entity.HDeliveryPlanDetailEntity;
import jp.co.jun.edi.entity.HDeliveryPlanEntity;
import jp.co.jun.edi.entity.HDeliveryPlanSkuEntity;
import jp.co.jun.edi.entity.TDeliveryPlanCutEntity;
import jp.co.jun.edi.entity.TDeliveryPlanDetailEntity;
import jp.co.jun.edi.entity.TDeliveryPlanEntity;
import jp.co.jun.edi.entity.TDeliveryPlanSkuEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryPlanCutModel;
import jp.co.jun.edi.model.DeliveryPlanDetailModel;
import jp.co.jun.edi.model.DeliveryPlanModel;
import jp.co.jun.edi.model.DeliveryPlanSkuModel;
import jp.co.jun.edi.model.mail.DeliveryPlanSendModel;
import jp.co.jun.edi.repository.HDeliveryPlanCutRepository;
import jp.co.jun.edi.repository.HDeliveryPlanDetailRepository;
import jp.co.jun.edi.repository.HDeliveryPlanRepository;
import jp.co.jun.edi.repository.HDeliveryPlanSkuRepository;
import jp.co.jun.edi.repository.TDeliveryPlanCutRepository;
import jp.co.jun.edi.repository.TDeliveryPlanDetailRepository;
import jp.co.jun.edi.repository.TDeliveryPlanRepository;
import jp.co.jun.edi.repository.TDeliveryPlanSkuRepository;
//PRD_0181 add JFE start
import jp.co.jun.edi.repository.TOrderRepository;
//PRD_0181 add JFE end
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.DeliveryPlanEntryStatusType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;


/**
 * 納品予定、納品予定明細、納品予定SKU、納品予定裁断、各履歴 登録処理.
 */
@Service
public class DeliveryPlanCreateService
extends GenericCreateService<CreateServiceParameter<DeliveryPlanModel>, CreateServiceResponse<DeliveryPlanModel>> {
    @Autowired
    private TDeliveryPlanRepository deliveryPlanRepository;

    @Autowired
    private TDeliveryPlanDetailRepository deliveryPlanDetailRepository;

    @Autowired
    private TDeliveryPlanSkuRepository deliveryPlanSkuRepository;

    @Autowired
    private TDeliveryPlanCutRepository deliveryPlanCutRepository;

    @Autowired
    private HDeliveryPlanRepository hDeliveryPlanRepository;

    @Autowired
    private HDeliveryPlanDetailRepository hDeliveryPlanDetailRepository;

    @Autowired
    private HDeliveryPlanSkuRepository hDeliveryPlanSkuRepository;

    @Autowired
    private HDeliveryPlanCutRepository hDeliveryPlanCutRepository;

    @Autowired
    private DeliveryPlanCreateMailDataComponent deliveryPlanCreateMailDataComponent;

    @Autowired
    private DeliveryPlanRegistSendMailComponent deliveryPlanRegistSendMailComponent;

    // PRD_0145 #10776 add JFE start
    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;
    // PRD_0145 #10776 add JFE end
	// PRD_0181 add JFE start
    @Autowired
    private TOrderRepository orderRepository;
    // PRD_0181 add JFE end

    @Override
    protected CreateServiceResponse<DeliveryPlanModel> execute(final CreateServiceParameter<DeliveryPlanModel> serviceParameter) {
        final DeliveryPlanModel deliveryPlanModel = serviceParameter.getItem();

        // 裁断リストがないor1つでもnull要素が入っている→エラー
        if (CollectionUtils.isEmpty(deliveryPlanModel.getDeliveryPlanCuts())
                || deliveryPlanModel.getDeliveryPlanCuts().contains(null)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_001));
        }
        // 納品予定明細リストがない→エラー
        if (CollectionUtils.isEmpty(deliveryPlanModel.getDeliveryPlanDetails())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_002));
        }

        // 納品予定をEntityにコピー
        final TDeliveryPlanEntity tDeliveryPlanEntity = new TDeliveryPlanEntity();
        BeanUtils.copyProperties(deliveryPlanModel, tDeliveryPlanEntity);

        // 納品予定登録
        deliveryPlanRepository.save(tDeliveryPlanEntity);

        // 納品予定履歴の登録
        final HDeliveryPlanEntity hDeliveryPlanEntity = new HDeliveryPlanEntity();
        BeanUtils.copyProperties(tDeliveryPlanEntity, hDeliveryPlanEntity);
        hDeliveryPlanRepository.save(hDeliveryPlanEntity);

        // 納品予定裁断の登録
        this.insertDeliveryPlanCut(deliveryPlanModel, tDeliveryPlanEntity);

        // 納品予定明細の登録
        this.insertDeliveryPlanDetail(deliveryPlanModel, tDeliveryPlanEntity);

        // PRD_0145 #10776 add JFE start
        // 費目04の場合は発注SKU情報更新
        final ExtendedTOrderEntity extendedTOrderEntity = orderComponent.getExtendedTOrder(deliveryPlanModel.getOrderId());
        // 費目取得
        final ExpenseItemType expenseItem = extendedTOrderEntity.getExpenseItem();
        if (expenseItem == ExpenseItemType.SEWING_ORDER) {
        	this.updateOrderSku(deliveryPlanModel, serviceParameter.getLoginUser().getUserId());
        }
        // PRD_0145 #10776 add JFE end

        // PRD_0181 add JFE start
        final BigDecimal newNecessaryLengthActual;
        // 実用尺に入力がある場合は発注情報更新
        // 入力値
        if (deliveryPlanModel.getNecessaryLengthActual() != null) {
        	newNecessaryLengthActual = formatNecessaryLengthActual(deliveryPlanModel.getNecessaryLengthActual());
        } else {
        	newNecessaryLengthActual = formatNecessaryLengthActual(BigDecimal.ZERO);
        }
        // DB値
        final BigDecimal oldNecessaryLengthActual = extendedTOrderEntity.getNecessaryLengthActual();
        if (!Objects.equals(newNecessaryLengthActual, oldNecessaryLengthActual)) {
            orderRepository.updateNecessaryLengthActual(deliveryPlanModel.getOrderId(), newNecessaryLengthActual, serviceParameter.getLoginUser().getUserId());
        }
        // PRD_0181 add JFE end

        // レスポンスに納品予定IDを設定
        deliveryPlanModel.setId(tDeliveryPlanEntity.getId());

        // 登録済みステータス時のみメール送信
        Integer entryStatusType = deliveryPlanModel.getEntryStatus();
        if (null != entryStatusType
                && DeliveryPlanEntryStatusType.REGISTERED.getValue() == entryStatusType) {
            // メール送信用Model 取得
            DeliveryPlanSendModel sendModel = deliveryPlanCreateMailDataComponent.getSendModel(tDeliveryPlanEntity);
            // メール送信
            deliveryPlanRegistSendMailComponent.sendMail(sendModel, serviceParameter.getLoginUser().getAccountName());
        }

        return CreateServiceResponse.<DeliveryPlanModel>builder().item(deliveryPlanModel).build();
    }

    /**
     * 納品予定明細登録処理.
     * @param deliveryPlanModel DeliveryPlanModel
     * @param tDeliveryPlanEntity TDeliveryPlanEntity
     */
    private void insertDeliveryPlanDetail(final DeliveryPlanModel deliveryPlanModel, final TDeliveryPlanEntity tDeliveryPlanEntity) {
        for (final DeliveryPlanDetailModel deliveryPlanDetailModel : deliveryPlanModel.getDeliveryPlanDetails()) {
            // 日付なし、SKUなしの場合、登録しない
            if (Objects.isNull(deliveryPlanDetailModel.getDeliveryPlanAt())
                    && CollectionUtils.isEmpty(deliveryPlanDetailModel.getDeliveryPlanSkus())) {
                continue;
            }
            // 日付あり、SKUなしの場合、エラー
            if (Objects.nonNull(deliveryPlanDetailModel.getDeliveryPlanAt())
                    && CollectionUtils.isEmpty(deliveryPlanDetailModel.getDeliveryPlanSkus())) {
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_003));
            }

            // 納品予定sku数がnullのものを除去
            List<DeliveryPlanSkuModel> filteredDeliveryPlanSkus = deliveryPlanDetailModel.getDeliveryPlanSkus().stream()
                    .filter(sku -> sku.getDeliveryPlanLot() != null).collect(Collectors.toList());

            // フィルター後のskusが空の場合、以降の処理をスキップ
            if (CollectionUtils.isEmpty(filteredDeliveryPlanSkus)) {
                continue;
            }

            final TDeliveryPlanDetailEntity tDeliveryPlanDetailEntity = new TDeliveryPlanDetailEntity();
            // データをコピー
            BeanUtils.copyProperties(deliveryPlanDetailModel, tDeliveryPlanDetailEntity);
            // 納品予定のIDをセット
            tDeliveryPlanDetailEntity.setDeliveryPlanId(tDeliveryPlanEntity.getId());
            deliveryPlanDetailRepository.save(tDeliveryPlanDetailEntity);
            // 納品予定明細履歴の登録
            final HDeliveryPlanDetailEntity hDeliveryPlanDetailEntity = new HDeliveryPlanDetailEntity();
            BeanUtils.copyProperties(tDeliveryPlanDetailEntity, hDeliveryPlanDetailEntity);
            hDeliveryPlanDetailRepository.save(hDeliveryPlanDetailEntity);

            // フィルター後の納品予定SKUをセット
            deliveryPlanDetailModel.setDeliveryPlanSkus(filteredDeliveryPlanSkus);
            // 納品予定SKUの登録
            this.insertDeliveryPlanSku(deliveryPlanDetailModel, tDeliveryPlanDetailEntity);
        }

    }

    /**
     * 納品予定SKU登録処理.
     * @param deliveryPlanDetailModel DeliveryPlanDetailModel
     * @param tDeliveryPlanDetailEntity TDeliveryPlanDetailEntity
     */
    private void insertDeliveryPlanSku(final DeliveryPlanDetailModel deliveryPlanDetailModel, final TDeliveryPlanDetailEntity tDeliveryPlanDetailEntity) {
        for (final DeliveryPlanSkuModel deliveryPlanSkuModel : deliveryPlanDetailModel.getDeliveryPlanSkus()) {
            final TDeliveryPlanSkuEntity tDeliveryPlanSkuEntity = new TDeliveryPlanSkuEntity();

            // データをコピー
            BeanUtils.copyProperties(deliveryPlanSkuModel, tDeliveryPlanSkuEntity);

            // 納品予定IDをセット
            tDeliveryPlanSkuEntity.setDeliveryPlanId(tDeliveryPlanDetailEntity.getDeliveryPlanId());
            // 納品予定明細IDをセット
            tDeliveryPlanSkuEntity.setDeliveryPlanDetailId(tDeliveryPlanDetailEntity.getId());

            deliveryPlanSkuRepository.save(tDeliveryPlanSkuEntity);

            // 納品予定SKU履歴の登録
            final HDeliveryPlanSkuEntity hDeliveryPlanSkuEntity = new HDeliveryPlanSkuEntity();
            BeanUtils.copyProperties(tDeliveryPlanSkuEntity, hDeliveryPlanSkuEntity);
            hDeliveryPlanSkuRepository.save(hDeliveryPlanSkuEntity);
        }
    }

    /**
     * 納品予定裁断登録処理.
     * @param deliveryPlanModel DeliveryPlanModel
     * @param tDeliveryPlanEntity TDeliveryPlanEntity
     */
    private void insertDeliveryPlanCut(final DeliveryPlanModel deliveryPlanModel, final TDeliveryPlanEntity tDeliveryPlanEntity) {
        for (final DeliveryPlanCutModel deliveryPlanCutModel : deliveryPlanModel.getDeliveryPlanCuts()) {
            final TDeliveryPlanCutEntity tDeliveryPlanCutEntity = new TDeliveryPlanCutEntity();

            // データをコピー
            BeanUtils.copyProperties(deliveryPlanCutModel, tDeliveryPlanCutEntity);

            // 納品予定のIDをセット
            tDeliveryPlanCutEntity.setDeliveryPlanId(tDeliveryPlanEntity.getId());

            deliveryPlanCutRepository.save(tDeliveryPlanCutEntity);

            // 納品予定裁断履歴の登録
            final HDeliveryPlanCutEntity hDeliveryPlanCutEntity = new HDeliveryPlanCutEntity();
            BeanUtils.copyProperties(tDeliveryPlanCutEntity, hDeliveryPlanCutEntity);
            hDeliveryPlanCutRepository.save(hDeliveryPlanCutEntity);
        }
    }

    // PRD_0145 #10776 add JFE start
    /**
     * 発注SKU更新処理.
     * @param deliveryPlanModel DeliveryPlanModel
     * @param tDeliveryPlanEntity TDeliveryPlanEntity
     */
    private void updateOrderSku(final DeliveryPlanModel deliveryPlanModel, final BigInteger loginUserId) {
        for (final DeliveryPlanCutModel deliveryPlanCutModel : deliveryPlanModel.getDeliveryPlanCuts()) {

        	// 発注SKU情報を取得
        	TOrderSkuEntity tOrderSkuEntity = orderSkuRepository.findByOrderIdAndColorAndSize(
        			deliveryPlanModel.getOrderId(), deliveryPlanCutModel.getColorCode(), deliveryPlanCutModel.getSize())
        			.orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("getOrderSkuInfo")
                            .message("t_order_sku not found.")
                            .build())));

            // DB値と入力値が異なる場合、製品裁断数を更新
        	if (!Objects.equals(tOrderSkuEntity.getProductCutLot(), deliveryPlanCutModel.getDeliveryPlanCutLot())) {
                orderSkuRepository.updateProductCutLotByOrderIdAndColorCodeAndSize(
                		deliveryPlanModel.getOrderId(), deliveryPlanCutModel.getColorCode(), deliveryPlanCutModel.getSize(), deliveryPlanCutModel.getDeliveryPlanCutLot(), loginUserId);
        	}
        }
    }
    // PRD_0145 #10776 add JFE end
	
	// PRD_0181 add JFE start
    /**
     * 実用尺入力値フォーマット.
     * @param necessaryLengthActual 実用尺入力値
     */
    private BigDecimal formatNecessaryLengthActual(final BigDecimal necessaryLengthActual) {
    	DecimalFormat format = new DecimalFormat("#.#");
    	// 小数点以下の最小値
    	format.setMinimumFractionDigits(3);
    	// 小数点以下の最大値
    	format.setMaximumFractionDigits(3);
    	BigDecimal bigDecimalNecessaryLengthActual = new BigDecimal(format.format(necessaryLengthActual));
    	return bigDecimalNecessaryLengthActual;
    }
    // PRD_0181 add JFE end
}
