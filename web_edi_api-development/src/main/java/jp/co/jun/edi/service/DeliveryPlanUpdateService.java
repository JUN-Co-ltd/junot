package jp.co.jun.edi.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryPlanCreateMailDataComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.component.mail.DeliveryPlanUpdateSendMailComponent;
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
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.DeliveryPlanEntryStatusType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;


/**
 * 納品予定、納品予定明細、納品予定SKU、納品予定裁断、各履歴 更新処理.
 */
@Service
public class DeliveryPlanUpdateService
extends GenericCreateService<UpdateServiceParameter<DeliveryPlanModel>, UpdateServiceResponse<DeliveryPlanModel>> {
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
    private DeliveryPlanUpdateSendMailComponent deliveryPlanUpdateSendMailComponent;

    // PRD_0145 #10776 add JFE start
    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;
    // PRD_0145 #10776 add JFE end

    @Override
    protected UpdateServiceResponse<DeliveryPlanModel> execute(final UpdateServiceParameter<DeliveryPlanModel> serviceParameter) {
        final DeliveryPlanModel deliveryPlanModel = serviceParameter.getItem();

        // DBから最新の納品予定を取得する
        final Optional<TDeliveryPlanEntity> deliveryPlanOptional = deliveryPlanRepository.findById(deliveryPlanModel.getId());
        // メール送信チェック用登録済ステータス
        Integer entryStatusTypeOfDB = deliveryPlanOptional.get().getEntryStatus();

        // 更新可否チェック
        checkCanUpdated(deliveryPlanModel, deliveryPlanOptional);

        // 納品予定の更新
        // 納品予定をEntityにコピー
        final TDeliveryPlanEntity tDeliveryPlanEntity = new TDeliveryPlanEntity();
        BeanUtils.copyProperties(deliveryPlanModel, tDeliveryPlanEntity);

        // 作成日、作成ユーザIDをクリア
        tDeliveryPlanEntity.setCreatedAt(null);
        tDeliveryPlanEntity.setCreatedUserId(null);

        // 納品予定更新
        deliveryPlanRepository.save(tDeliveryPlanEntity);

        // 納品予定履歴の登録
        final HDeliveryPlanEntity hDeliveryPlanEntity = new HDeliveryPlanEntity();
        BeanUtils.copyProperties(tDeliveryPlanEntity, hDeliveryPlanEntity);
        hDeliveryPlanRepository.save(hDeliveryPlanEntity);

        // 納品予定裁断の更新
        this.updateDeliveryPlanCut(deliveryPlanModel, tDeliveryPlanEntity);

        // 納品予定明細の更新
        this.updateDeliveryPlanDetail(deliveryPlanModel, tDeliveryPlanEntity, serviceParameter.getLoginUser().getUserId());

        // PRD_0145 #10776 add JFE start
        // 費目04の場合は発注SKU情報更新
        final ExtendedTOrderEntity extendedTOrderEntity = orderComponent.getExtendedTOrder(deliveryPlanModel.getOrderId());
        // 費目取得
        final ExpenseItemType expenseItem = extendedTOrderEntity.getExpenseItem();
        if (expenseItem == ExpenseItemType.SEWING_ORDER) {
        	this.updateOrderSku(deliveryPlanModel, serviceParameter.getLoginUser().getUserId());
        }

    	// PRD_0181 mod JFE start
        //BigDecimal newNecessaryLengthActual;
    	final BigDecimal newNecessaryLengthActual;
        // PRD_0181 mod JFE end
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
        // PRD_0145 #10776 add JFE end

        // 確認モーダルの「全ての納品予定明細を入力済」にチェックされた場合、
        // もしくは既に「全ての納品予定明細を入力済」の状態の場合は送信
        Integer entryStatusType = deliveryPlanModel.getEntryStatus();
        if ((null != entryStatusType && DeliveryPlanEntryStatusType.REGISTERED.getValue() == entryStatusType)
            || (null != entryStatusTypeOfDB && DeliveryPlanEntryStatusType.REGISTERED.getValue() == entryStatusTypeOfDB)) {
            // メール送信用Model 取得
            DeliveryPlanSendModel sendModel = deliveryPlanCreateMailDataComponent.getSendModel(tDeliveryPlanEntity);
            // メール送信
            deliveryPlanUpdateSendMailComponent.sendMail(sendModel, serviceParameter.getLoginUser().getAccountName());
        }

        return UpdateServiceResponse.<DeliveryPlanModel>builder().item(deliveryPlanModel).build();
    }

    /**
     * 納品予定明細更新処理.
     * @param deliveryPlanModel DeliveryPlanModel
     * @param tDeliveryPlanEntity TDeliveryPlanEntity
     * @param loginUserId BigInteger
     */
    private void updateDeliveryPlanDetail(final DeliveryPlanModel deliveryPlanModel,
            final TDeliveryPlanEntity tDeliveryPlanEntity, final BigInteger loginUserId) {
        final List<BigInteger> detailIdList = new ArrayList<>();

        TDeliveryPlanDetailEntity tDeliveryPlanDetailEntity;
        for (final DeliveryPlanDetailModel deliveryPlanDetailModel : deliveryPlanModel.getDeliveryPlanDetails()) {
            // 日付あり、SKUなしの場合、エラー
            if (Objects.nonNull(deliveryPlanDetailModel.getDeliveryPlanAt())
                    && CollectionUtils.isEmpty(deliveryPlanDetailModel.getDeliveryPlanSkus())) {
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_003));
            }

            // 納品予定sku数が0のものを除去
            List<DeliveryPlanSkuModel> filteredDeliveryPlanSkus = deliveryPlanDetailModel.getDeliveryPlanSkus().stream()
                    .filter(sku -> sku.getDeliveryPlanLot() != null).collect(Collectors.toList());

            // フィルター後のskusが空の場合、以降の処理をスキップ
            if (CollectionUtils.isEmpty(filteredDeliveryPlanSkus)) {
                continue;
            }

            tDeliveryPlanDetailEntity = new TDeliveryPlanDetailEntity();

            // データをコピー
            BeanUtils.copyProperties(deliveryPlanDetailModel, tDeliveryPlanDetailEntity);

            // 納品予定のIDをセット
            tDeliveryPlanDetailEntity.setDeliveryPlanId(tDeliveryPlanEntity.getId());

            final TDeliveryPlanDetailEntity returnEntity = deliveryPlanDetailRepository.save(tDeliveryPlanDetailEntity);
            // 更新・登録したIDはリストに格納
            detailIdList.add(returnEntity.getId());

            // 納品予定明細履歴の登録
            final HDeliveryPlanDetailEntity hDeliveryPlanDetailEntity = new HDeliveryPlanDetailEntity();
            BeanUtils.copyProperties(tDeliveryPlanDetailEntity, hDeliveryPlanDetailEntity);
            hDeliveryPlanDetailRepository.save(hDeliveryPlanDetailEntity);

            // フィルター後の納品予定SKUをセット
            deliveryPlanDetailModel.setDeliveryPlanSkus(filteredDeliveryPlanSkus);
            // 納品予定SKUの更新
            this.updateDeliveryPlanSku(deliveryPlanDetailModel, tDeliveryPlanDetailEntity, loginUserId);
        }
        // 0件の場合は、存在しないID(0)を設定する
        if (detailIdList.size() == 0) {
            detailIdList.add(BigInteger.ZERO);
        }

        // リストに格納したID以外のレコードに削除日を設定する
        deliveryPlanDetailRepository.updateDeleteAtByDeliveryPlanIdAndIds(deliveryPlanModel.getId(), detailIdList, loginUserId);

    }

    /**
     * 納品予定SKU更新処理.
     * @param deliveryPlanDetailModel DeliveryPlanDetailModel
     * @param tDeliveryPlanDetailEntity TDeliveryPlanDetailEntity
     * @param loginUserId BigInteger
     */
    private void updateDeliveryPlanSku(final DeliveryPlanDetailModel deliveryPlanDetailModel,
            final TDeliveryPlanDetailEntity tDeliveryPlanDetailEntity, final BigInteger loginUserId) {
        final List<BigInteger> skuIdList = new ArrayList<>();

        TDeliveryPlanSkuEntity tDeliveryPlanSkuEntity;
        for (final DeliveryPlanSkuModel deliveryPlanSkuModel : deliveryPlanDetailModel.getDeliveryPlanSkus()) {

            tDeliveryPlanSkuEntity = new TDeliveryPlanSkuEntity();

            // データをコピー
            BeanUtils.copyProperties(deliveryPlanSkuModel, tDeliveryPlanSkuEntity);

            // 納品予定IDをセット
            tDeliveryPlanSkuEntity.setDeliveryPlanId(tDeliveryPlanDetailEntity.getDeliveryPlanId());
            // 納品予定明細IDをセット
            tDeliveryPlanSkuEntity.setDeliveryPlanDetailId(tDeliveryPlanDetailEntity.getId());

            final TDeliveryPlanSkuEntity returnEntity = deliveryPlanSkuRepository.save(tDeliveryPlanSkuEntity);
            // 更新・登録したIDはリストに格納
            skuIdList.add(returnEntity.getId());

            // 納品予定SKU履歴の登録
            final HDeliveryPlanSkuEntity hDeliveryPlanSkuEntity = new HDeliveryPlanSkuEntity();
            BeanUtils.copyProperties(tDeliveryPlanSkuEntity, hDeliveryPlanSkuEntity);
            hDeliveryPlanSkuRepository.save(hDeliveryPlanSkuEntity);
        }
        // 0件の場合は、存在しないID(0)を設定する
        if (skuIdList.size() == 0) {
            skuIdList.add(BigInteger.ZERO);
        }

        // リストに格納したID以外のレコードに削除日を設定する
        deliveryPlanSkuRepository.updateDeleteAtByDeliveryPlanDetailIdAndIds(deliveryPlanDetailModel.getId(), skuIdList, loginUserId);
    }

    /**
     * 納品予定裁断更新処理.
     * @param deliveryPlanModel DeliveryPlanModel
     * @param tDeliveryPlanEntity TDeliveryPlanEntity
     */
    private void updateDeliveryPlanCut(final DeliveryPlanModel deliveryPlanModel, final TDeliveryPlanEntity tDeliveryPlanEntity) {
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
    // PRD_0145 #10776 add JFE end

    /**
     * UPDATEが可能かチェックを行う.
     * @param deliveryPlanModel 更新対象の納品予定
     * @param deliveryPlanOptional 最新のDBの納品予定
     */
    private void checkCanUpdated(final DeliveryPlanModel deliveryPlanModel, final Optional<TDeliveryPlanEntity> deliveryPlanOptional) {
        // 削除・存在チェック
        existenceCheck(deliveryPlanOptional);

        // 裁断リストがないor1つでもnull要素が入っている→エラー
        if (CollectionUtils.isEmpty(deliveryPlanModel.getDeliveryPlanCuts())
                || deliveryPlanModel.getDeliveryPlanCuts().contains(null)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_001));
        }
        // 納品予定明細リストがない→エラー
        if (CollectionUtils.isEmpty(deliveryPlanModel.getDeliveryPlanDetails())) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_P_002));
        }

    }

    /**
     * 削除・存在チェック.
     * @param deliveryPlanOptional 最新のDBの納品予定
     */
    private void existenceCheck(final Optional<TDeliveryPlanEntity> deliveryPlanOptional) {

        if (!deliveryPlanOptional.isPresent() || deliveryPlanOptional.get().getDeletedAt() != null) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }
    }
}
