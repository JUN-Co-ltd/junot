package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruOrderComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.entity.TFItemEntity;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.entity.TFOrderSkuEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSkuModel;
import jp.co.jun.edi.repository.TFItemRepository;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TFOrderSkuRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * フクキタル品番情報を作成するサービス.
 */
@Service
public class FukukitaruOrderCreateService
        extends GenericCreateService<CreateServiceParameter<FukukitaruOrderModel>, CreateServiceResponse<FukukitaruOrderModel>> {

    @Autowired
    private TFOrderRepository tfOrderRepository;
    @Autowired
    private TFOrderSkuRepository tfOrderSkuRepository;
    @Autowired
    private TOrderRepository tOrderRepository;
    @Autowired
    private TItemRepository tItemRepository;
    @Autowired
    private TFItemRepository tfItemRepository;
    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;
    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected CreateServiceResponse<FukukitaruOrderModel> execute(final CreateServiceParameter<FukukitaruOrderModel> serviceParameter) {
        final FukukitaruOrderModel fukukitaruOrderModel = serviceParameter.getItem();
        final CustomLoginUser user = serviceParameter.getLoginUser();

        // バリデーションチェック
        final ResultMessages rsltMsg = checkValidate(fukukitaruOrderModel);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
        // フクキタル品番情報を取得
        TFItemEntity tfItemEntity = tfItemRepository.findByPartNoId(fukukitaruOrderModel.getPartNoId())
                .orElseThrow(() -> new BusinessException(rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001))));
        fukukitaruOrderModel.setFItemId(tfItemEntity.getId()); // フクキタル品番情報
        fukukitaruOrderModel.setOrderUserId(user.getUserId()); // 発注者ユーザID（ログインユーザID）

        // フクキタル発注情報の登録
        final TFOrderEntity tfOrderEntity = setValueForTFOrderEntity(fukukitaruOrderModel, user);
        tfOrderRepository.save(tfOrderEntity);
        // オーダー識別コードを生成。 {品番ID}_{発注ID}_{フクキタル発注ID}
        final String orderCode = String.format("%s_%s_%s", tfOrderEntity.getPartNoId().toString(), tfOrderEntity.getOrderId().toString(),
                tfOrderEntity.getId().toString());
        tfOrderEntity.setOrderCode(orderCode);
        // オーダー識別コードを更新
        tfOrderRepository.updateOrderCode(tfOrderEntity.getId(), tfOrderEntity.getOrderCode());

        // フクキタル発注SKU情報の登録
        final List<TFOrderSkuEntity> listTFOrderSkuEntity = new ArrayList<TFOrderSkuEntity>();
        listTFOrderSkuEntity.addAll(
                setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuAttentionName(), FukukitaruMasterMaterialType.ATTENTION_NAME));
        listTFOrderSkuEntity.addAll(
                setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuAttentionTag(), FukukitaruMasterMaterialType.ATTENTION_TAG));
        listTFOrderSkuEntity.addAll(
                setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBill(), FukukitaruMasterMaterialType.HANG_TAG));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillAttention(),
                FukukitaruMasterMaterialType.ATTENTION_HANG_TAG));
        listTFOrderSkuEntity
                .addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillAuxiliaryMaterial(),
                        FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuWashAuxiliary(),
                FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillNergyMerit(),
                FukukitaruMasterMaterialType.HANG_TAG_NERGY_MERIT));
        listTFOrderSkuEntity
                .addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuWashName(), FukukitaruMasterMaterialType.WASH_NAME));
        tfOrderSkuRepository.saveAll(listTFOrderSkuEntity);

        // レスポンスにフクキタル発注IDを設定
        fukukitaruOrderModel.setId(tfOrderEntity.getId());
        // レスポンスにオーダー識別コードを設定
        fukukitaruOrderModel.setOrderCode(tfOrderEntity.getOrderCode());

        return CreateServiceResponse.<FukukitaruOrderModel>builder().item(fukukitaruOrderModel).build();
    }

    /**
     * 入力されたフクキタル発注情報を登録用のフクキタル発注情報に詰め替え.
     * @param fukukitaruOrderModel 画面から入力されたフクキタル発注情報
     * @param user ログインユーザ情報
     * @return 登録用のフクキタル発注情報
     */
    private TFOrderEntity setValueForTFOrderEntity(final FukukitaruOrderModel fukukitaruOrderModel, final CustomLoginUser user) {
        // ID
        fukukitaruOrderModel.setId(null);
        // オーダー識別コード.フクキタル発注IDが付与後にアップデートするため、空文字を設定する
        fukukitaruOrderModel.setOrderCode("");
        // 確定ステータス.承認必要な場合は2(未承認)をセット、それ以外は0(未確定)をセット
        fukukitaruOrderComponent.resetConfirmStatus(fukukitaruOrderModel);
        // 責任発注の再セット
        fukukitaruOrderModel.setIsResponsibleOrder(BooleanType.FALSE);
        // 連携ステータスの再セット
        fukukitaruOrderModel.setLinkingStatus(FukukitaruMasterLinkingStatusType.TARGET);
        // 発注メール送信日
        fukukitaruOrderModel.setOrderSendAt(null);
        return fukukitaruOrderComponent.setValueForTFOrderEntity(fukukitaruOrderModel, user);

    }

    /**
     * 入力されたフクキタル発注SKU情報を登録用のフクキタル発注SKU情報に詰め替え.
     * @param fOrderId フクキタル発注ID
     * @param listFkOrderSkuModel 画面から入力されたフクキタル発注SKU情報
     * @param materialType 資材種別
     * @return 登録用のフクキタル発注SKU情報
     */
    private List<TFOrderSkuEntity> setValueForOrderSkuEntity(final BigInteger fOrderId, final List<FukukitaruOrderSkuModel> listFkOrderSkuModel,
            final FukukitaruMasterMaterialType materialType) {
        if (Objects.isNull(listFkOrderSkuModel)) {
            // NULLの場合、空のリストを返す
            return new ArrayList<TFOrderSkuEntity>();
        }
        return listFkOrderSkuModel.stream().map(model -> {
            final TFOrderSkuEntity entity = new TFOrderSkuEntity();
            entity.setColorCode(model.getColorCode());
            entity.setSize(model.getSize());
            entity.setMaterialId(model.getMaterialId());
            entity.setOrderLot(model.getOrderLot());
            entity.setSortOrder(model.getSortOrder());

            // カラーコードがNULLの場合、空文字を設定する
            if (Objects.isNull(entity.getColorCode())) {
                entity.setColorCode("");
            }
            // サイズがNULLの場合、空文字を設定する
            if (Objects.isNull(entity.getSize())) {
                entity.setSize("");
            }
            // 新規登録のためIDにNULLを設定
            entity.setId(null);
            // フクキタル発注ID
            entity.setFOrderId(fOrderId);

            // 資材種別
            entity.setMaterialType(materialType);

            // 並び順
            entity.setSortOrder(model.getSortOrder());

            return entity;
        }).collect(Collectors.toList());
    }

    /**
     * バリデーションチェック.
     * @param fukukitaruOrderModel フクキタル発注情報
     * @return ResultMessages
     */
    private ResultMessages checkValidate(final FukukitaruOrderModel fukukitaruOrderModel) {
        ResultMessages rsltMsg = ResultMessages.warning();

        // 品番情報取得
        final Optional<TItemEntity> tItemEntityOptional = tItemRepository.findByIdAndDeletedAtIsNull(fukukitaruOrderModel.getPartNoId());

        // 品番情報が削除されている場合は登録不可
        if (!tItemEntityOptional.isPresent()) {
            return rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001));

        }

        // 外部連携区分:JUNoT登録以外の場合更新不可
        itemComponent.validateReadOnly(tItemEntityOptional.get().getExternalLinkingType());

        // 発注情報が削除されている場合は登録不可
        if (!tOrderRepository.findByOrderId(fukukitaruOrderModel.getOrderId()).isPresent()) {
            return rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001));

        }
        return rsltMsg;
    }
}
