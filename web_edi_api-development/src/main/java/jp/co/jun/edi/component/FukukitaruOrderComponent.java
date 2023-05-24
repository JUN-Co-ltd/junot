package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MFDestinationEntity;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruDestinationModel;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSkuModel;
import jp.co.jun.edi.repository.MFDestinationRepository;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TFOrderSkuRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTFOrderSkuRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;

/**
 * フクキタル発注情報関連のコンポーネント.
 */
@Component
public class FukukitaruOrderComponent extends GenericComponent {

    @Autowired
    private TFOrderRepository tfOrderRepository;

    @Autowired
    private TFOrderSkuRepository tfOrderSkuRepository;

    @Autowired
    private ExtendedTFOrderSkuRepository extendedTFOrderSkuRepository;

    @Autowired
    private MFDestinationRepository mfDestinationRepository;

    @Autowired
    private TOrderRepository tOrderRepository;

    /**
     * フクキタル品番情報に紐づく、フクキタル発注情報の削除.
     *
     * @param fItemId
     *            フクキタル品番ID
     */
    public void delete(final BigInteger fItemId) {
        tfOrderRepository.findByFItemId(fItemId, PageRequest.of(0, Integer.MAX_VALUE)).forEach(tfOrderEntity -> {
            delete(tfOrderEntity);
        });
    }

    /**
     * フクキタル発注情報の削除.
     *
     * @param tfOrderEntity
     *            フクキタル発注情報
     */
    public void delete(final TFOrderEntity tfOrderEntity) {

        final BigInteger fOrderId = tfOrderEntity.getId();

        final Date now = DateUtils.createNow();

        // フクキタル発注情報を削除
        tfOrderRepository.updateDeleteAtByFOrderId(fOrderId, now);

        // フクキタル発注SKU情報を削除
        tfOrderSkuRepository.updateDeleteAtByFOrderId(fOrderId, now);
    }

    /**
     * {@link FukukitaruOrderModel} を {@link TFOrderEntity} に詰め替え.
     *
     * @param fukukitaruOrderModel
     *            {@link FukukitaruOrderModel} instance
     * @param user
     *            ログインユーザ情報
     * @return {@link TFOrderEntity} instance
     */
    public TFOrderEntity setValueForTFOrderEntity(final FukukitaruOrderModel fukukitaruOrderModel, final CustomLoginUser user) {
        final TFOrderEntity entity = new TFOrderEntity();
        entity.setId(fukukitaruOrderModel.getId());
        entity.setFItemId(fukukitaruOrderModel.getFItemId());
        entity.setPartNoId(fukukitaruOrderModel.getPartNoId());
        entity.setOrderId(fukukitaruOrderModel.getOrderId());
        entity.setBillingCompanyId(fukukitaruOrderModel.getBillingCompanyId());
        entity.setContractNumber(fukukitaruOrderModel.getContractNumber());
        entity.setDeliveryCompanyId(fukukitaruOrderModel.getDeliveryCompanyId());
        entity.setDeliveryStaff(fukukitaruOrderModel.getDeliveryStaff());
        entity.setOrderAt(fukukitaruOrderModel.getOrderAt());
        entity.setOrderCode(fukukitaruOrderModel.getOrderCode());
        entity.setOrderUserId(fukukitaruOrderModel.getOrderUserId());
        entity.setPreferredShippingAt(fukukitaruOrderModel.getPreferredShippingAt());
        entity.setRepeatNumber(fukukitaruOrderModel.getRepeatNumber());
        entity.setSpecialReport(fukukitaruOrderModel.getSpecialReport());
        entity.setUrgent(fukukitaruOrderModel.getUrgent());
        entity.setDeliveryType(fukukitaruOrderModel.getDeliveryType());
        entity.setConfirmStatus(fukukitaruOrderModel.getConfirmStatus());
        entity.setOrderSendAt(fukukitaruOrderModel.getOrderSendAt());
        entity.setOrderType(fukukitaruOrderModel.getOrderType());
        entity.setIsResponsibleOrder(fukukitaruOrderModel.getIsResponsibleOrder());
        entity.setLinkingStatus(fukukitaruOrderModel.getLinkingStatus());
        entity.setRemarks(fukukitaruOrderModel.getRemarks());

        // 契約NoがNULLの場合、空文字を設定する
        if (Objects.isNull(entity.getContractNumber())) {
            entity.setContractNumber("");
        }
        // 納入先担当者がNULLの場合、空文字を設定する
        if (Objects.isNull(entity.getDeliveryStaff())) {
            entity.setDeliveryStaff("");
        }
        // 特記事項がNULLの場合、空文字を設定する
        if (Objects.isNull(entity.getSpecialReport())) {
            entity.setSpecialReport("");
        }
        // 工場No.（発注情報の生産メーカーコード）
        final TOrderEntity tOrderEntity = tOrderRepository.findById(fukukitaruOrderModel.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
        entity.setMdfMakerFactoryCode(tOrderEntity.getMdfMakerCode());
        return entity;
    }

    /**
     * {@link TFOrderEntity} を {@link FukukitaruOrderModel} に詰め替え. {@link TFOrderEntity.billingCompanyId}から請求宛先情報を取得し、{@link FukukitaruOrderModel} に入れる.
     * {@link TFOrderEntity.deliveryCompanyId}から発注宛先情報を取得し、{@link FukukitaruOrderModel} に入れる.
     *
     * @param entity
     *            {link TFOrderEntity} instance
     * @return {@link FukukitaruOrderModel} instance
     */
    public FukukitaruOrderModel setTFOrderEntityForModel(final TFOrderEntity entity) {
        final FukukitaruOrderModel model = new FukukitaruOrderModel();
        BeanUtils.copyProperties(entity, model);

        // 請求先詳細情報を取得し、モデルに変換する
        final Optional<MFDestinationEntity> optionalBillAddressEntity = mfDestinationRepository.findById(entity.getBillingCompanyId());
        if (optionalBillAddressEntity.isPresent()) {
            final FukukitaruDestinationModel billingAddressModel = new FukukitaruDestinationModel();
            BeanUtils.copyProperties(optionalBillAddressEntity.get(), billingAddressModel);
            // 請求先ID
            model.setBillingDestination(billingAddressModel);
            // 承認需要フラグ
            model.setIsApprovalRequired(billingAddressModel.getIsApprovalRequired());
        }

        // 納品先詳細情報し、モデルに変換する
        final Optional<MFDestinationEntity> optionalDeliveryAddressEntity = mfDestinationRepository.findById(entity.getDeliveryCompanyId());
        if (optionalDeliveryAddressEntity.isPresent()) {
            final FukukitaruDestinationModel deliveryAddressModel = new FukukitaruDestinationModel();
            BeanUtils.copyProperties(optionalDeliveryAddressEntity.get(), deliveryAddressModel);
            // 納入先ID
            model.setDeliveryDestination(deliveryAddressModel);
        }
        return model;
    }

    /**
     * フクキタル発注SKU情報を取得し、{@link FukukitaruOrderModel} に入れる.
     *
     * @param fukukitaruOrderModel
     *            {@link FukukitaruOrderModel} instance
     */
    public void setFOrderSkuEntityForModel(final FukukitaruOrderModel fukukitaruOrderModel) {
        // 1:洗濯ネーム
        fukukitaruOrderModel.setOrderSkuWashName(getTFOrderSkuJoinMaterialWashName(fukukitaruOrderModel.getId()));
        // 2:アテンションネーム
        fukukitaruOrderModel.setOrderSkuAttentionName(getTFOrderSkuJoinMaterialAttentionName(fukukitaruOrderModel.getId()));
        // 3:洗濯同封副資材
        fukukitaruOrderModel.setOrderSkuWashAuxiliary(getTFOrderSkuJoinMaterialWashAuxiliary(fukukitaruOrderModel.getId()));
        // 4:下札
        fukukitaruOrderModel.setOrderSkuBottomBill(getTFOrderSkuJoinMaterialHangTag(fukukitaruOrderModel.getId()));
        // 5:アテンションタグ
        fukukitaruOrderModel.setOrderSkuAttentionTag(getTFOrderSkuJoinMaterialAttentionTag(fukukitaruOrderModel.getId()));
        // 6:アテンション下札
        fukukitaruOrderModel.setOrderSkuBottomBillAttention(getTFOrderSkuJoinMaterialAttentionHangTag(fukukitaruOrderModel.getId()));
        // 7:NERGY用メリット下札
        fukukitaruOrderModel.setOrderSkuBottomBillNergyMerit(getTFOrderSkuJoinMaterialHangTagNergyMerit(fukukitaruOrderModel.getId()));
        // 8:下札同封副資材
        fukukitaruOrderModel.setOrderSkuBottomBillAuxiliaryMaterial(getTFOrderSkuJoinMaterialHangTagAuxiliary(fukukitaruOrderModel.getId()));
    }

    /**
     * アテンションネームのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションネームのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialAttentionName(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialAttentionName(tfOrderId, FukukitaruMasterMaterialType.ATTENTION_NAME.getValue(),
                PageRequest.of(0, Integer.MAX_VALUE)).stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialAttentionTag(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialAttentionTag(tfOrderId, FukukitaruMasterMaterialType.ATTENTION_TAG.getValue(), PageRequest.of(0, Integer.MAX_VALUE))
                .stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * 下札のフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return 下札のフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialHangTag(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialHangTag(tfOrderId, FukukitaruMasterMaterialType.HANG_TAG.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialHangTagAuxiliary(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialHangTagAuxiliary(tfOrderId,
                FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialAttentionHangTag(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialAttentionHangTag(tfOrderId, FukukitaruMasterMaterialType.ATTENTION_HANG_TAG.getValue(),
                PageRequest.of(0, Integer.MAX_VALUE)).stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialWashName(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository
                .findByFOrderIdJoinMaterialWashName(tfOrderId, FukukitaruMasterMaterialType.WASH_NAME.getValue(), PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialWashAuxiliary(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialWashAuxiliary(tfOrderId, FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL.getValue(),
                PageRequest.of(0, Integer.MAX_VALUE)).stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * アテンションタグのフクキタル発注情報を取得し、{@link FukukitaruOrderSkuModel}に変換する.
     *
     * @param tfOrderId
     *            フクキタル発注ID
     * @return アテンションタグのフクキタル発注情報リスト
     */
    private List<FukukitaruOrderSkuModel> getTFOrderSkuJoinMaterialHangTagNergyMerit(final BigInteger tfOrderId) {
        return extendedTFOrderSkuRepository.findByFOrderIdJoinMaterialHangTagNergyMerit(tfOrderId, FukukitaruMasterMaterialType.HANG_TAG_NERGY_MERIT.getValue(),
                PageRequest.of(0, Integer.MAX_VALUE)).stream().map(entity -> {
                    final FukukitaruOrderSkuModel model = new FukukitaruOrderSkuModel();
                    BeanUtils.copyProperties(entity, model);
                    return model;
                }).collect(Collectors.toList());
    }

    /**
     * フクキタル発注の合計発注数を取得.
     *
     * @param orderType
     *            {@link FukukitaruMasterOrderType}
     * @param fOrderId
     *            フクキタル発注ID
     * @return 合計発注数
     */
    public int getTotalOrderLot(final FukukitaruMasterOrderType orderType, final BigInteger fOrderId) {
        // 合計発注数
        BigDecimal totalOrderLot = null;

        switch (orderType) {
        case WASH_NAME:
        case WASH_NAME_KOMONO:
            // 発注種別が洗濯ネーム、または、洗濯ネーム小物の場合、洗濯ネームの発注合計数を取得
            totalOrderLot = extendedTFOrderSkuRepository.totalCountWashName(fOrderId);
            break;
        case HANG_TAG:
        case HANG_TAG_KOMONO:
            // 発注種別が下札、または、下札小物の場合、下札の発注合計数を取得
            totalOrderLot = extendedTFOrderSkuRepository.totalCountHangTag(fOrderId);
            break;
        default:
            break;
        }

        if (Objects.isNull(totalOrderLot)) {
            return 0;
        }

        return totalOrderLot.intValue();
    }

    /**
     * 発注IDに紐づくフクキタル発注が1件以上確定しているか判別する.
     *
     * @param orderId
     *            発注ID
     * @return 1件以上確定してる場合はtrue、そうでない場合はfalseを返す
     */
    public boolean existsMaterialOrderConfirm(final BigInteger orderId) {
        if (tfOrderRepository.findByOrderId(orderId, PageRequest.of(0, Integer.MAX_VALUE)).stream()
                .filter(entity -> entity.getConfirmStatus() == FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED).count() == 0) {
            // フクキタル発注情報なし、または、発注確定が0件の場合falseを返す
            return false;
        }
        return true;
    }

    /**
     * 確定ステータスの再設定.
     *
     * 承認必要である場合(請求先がJUN)、確定ステータスを2(未承認)にセット 承認不要の場合、確定ステータスを0(未確定)にセット
     *
     * @param fukukitaruOrderModel
     *            フクキタル資材発注情報
     */
    public void resetConfirmStatus(final FukukitaruOrderModel fukukitaruOrderModel) {
        if (fukukitaruOrderModel.getIsApprovalRequired() == BooleanType.TRUE) {
            // 承認必要は2(未承認)をセット
            fukukitaruOrderModel.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_UNAPPROVED);
        } else {
            // 承認不要は0(未確定)をセット
            fukukitaruOrderModel.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_NOT_CONFIRMED);
        }
    }
}
