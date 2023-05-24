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
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.entity.TFOrderSkuEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSkuModel;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.repository.TFOrderSkuRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;

/**
 * フクキタル発注情報を確定するサービス.
 */
@Service
public class FukukitaruOrderConfirmService
        extends GenericUpdateService<UpdateServiceParameter<FukukitaruOrderModel>, UpdateServiceResponse<FukukitaruOrderModel>> {
    @Autowired
    private TFOrderRepository tfOrderRepository;

    @Autowired
    private TFOrderSkuRepository tfOrderSkuRepository;

    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private TItemRepository tItemRepository;

    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected UpdateServiceResponse<FukukitaruOrderModel> execute(final UpdateServiceParameter<FukukitaruOrderModel> serviceParameter) {
        final FukukitaruOrderModel fukukitaruOrderModel = serviceParameter.getItem();
        final CustomLoginUser user = serviceParameter.getLoginUser();

        // DBから最新のフクキタル発注情報を取得する。削除されている場合は、例外をスローする
        final TFOrderEntity currentTFOrderEntity = tfOrderRepository.findByIdDeletedAtIsNull(fukukitaruOrderModel.getId())
                .orElseThrow(() -> new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001))));

        // バリデーションチェック
        checkValidate(currentTFOrderEntity, fukukitaruOrderModel);

        // フクキタル発注情報をEntityにコピー
        final TFOrderEntity tfOrderEntity = setValueForTFOrderEntity(currentTFOrderEntity, fukukitaruOrderModel, user);

        // フクキタル発注情報を更新
        tfOrderRepository.save(tfOrderEntity);

        final List<TFOrderSkuEntity> listTFOrderSkuEntity = new ArrayList<TFOrderSkuEntity>();
        listTFOrderSkuEntity.addAll(
                setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuAttentionName(), FukukitaruMasterMaterialType.ATTENTION_NAME));
        listTFOrderSkuEntity.addAll(
                setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuAttentionTag(), FukukitaruMasterMaterialType.ATTENTION_TAG));
        listTFOrderSkuEntity
                .addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBill(), FukukitaruMasterMaterialType.HANG_TAG));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillAttention(),
                FukukitaruMasterMaterialType.ATTENTION_HANG_TAG));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillAuxiliaryMaterial(),
                FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuWashAuxiliary(),
                FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL));
        listTFOrderSkuEntity.addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuBottomBillNergyMerit(),
                FukukitaruMasterMaterialType.HANG_TAG_NERGY_MERIT));
        listTFOrderSkuEntity
                .addAll(setValueForOrderSkuEntity(tfOrderEntity.getId(), fukukitaruOrderModel.getOrderSkuWashName(), FukukitaruMasterMaterialType.WASH_NAME));
        tfOrderSkuRepository.saveAll(listTFOrderSkuEntity);

        List<BigInteger> ids = listTFOrderSkuEntity.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            // 登録したID以外のレコードに削除日を設定する
            tfOrderSkuRepository.updateByFOrderIdNotInIds(tfOrderEntity.getId(),
                    ids,
                    DateUtils.createNow());
        }

        return UpdateServiceResponse.<FukukitaruOrderModel>builder().item(fukukitaruOrderModel).build();
    }

    /**
     * バリデーションチェック.
     * @param currentTFOrderEntity DBに登録されているフクキタル発注情報
     * @param fukukitaruOrderModel 画面から送信されてきたフクキタル発注情報
     */
    private void checkValidate(final TFOrderEntity currentTFOrderEntity, final FukukitaruOrderModel fukukitaruOrderModel) {
        // 品番情報取得
        final Optional<TItemEntity> tItemEntityOptional = tItemRepository.findByIdAndDeletedAtIsNull(fukukitaruOrderModel.getPartNoId());

        // 品番情報が削除されている場合は登録不可
        if (!tItemEntityOptional.isPresent()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001)));
        }

        // 外部連携区分:JUNoT登録以外の場合更新不可
        itemComponent.validateReadOnly(tItemEntityOptional.get().getExternalLinkingType());

        // 発注情報が削除されている場合は登録不可
        if (!tOrderRepository.findByOrderId(fukukitaruOrderModel.getOrderId()).isPresent()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_001)));
        }
        // 確定の場合は登録不可
        if (FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED == currentTFOrderEntity.getConfirmStatus()) {
            throw new BusinessException(ResultMessages.warning().add(ResultMessage.fromCode(MessageCodeType.CODE_FO_003)));
        }
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

            entity.setId(model.getId());
            entity.setColorCode(model.getColorCode());
            entity.setSize(model.getSize());
            entity.setMaterialId(model.getMaterialId());
            entity.setOrderLot(model.getOrderLot());
            entity.setMaterialType(materialType);
            entity.setSortOrder(model.getSortOrder());

            // カラーコードがNULLの場合、空文字を設定する
            if (Objects.isNull(entity.getColorCode())) {
                entity.setColorCode("");
            }
            // サイズがNULLの場合、空文字を設定する
            if (Objects.isNull(entity.getSize())) {
                entity.setSize("");
            }
            // フクキタル発注ID
            entity.setFOrderId(fOrderId);

            return entity;
        }).collect(Collectors.toList());
    }

    /**
     * 入力されたフクキタル発注情報を登録用のフクキタル発注情報に詰め替え.
     * @param currentTFOrderEntity DBに登録されているフクキタル発注情報
     * @param fukukitaruOrderModel 画面から入力されたフクキタル発注情報
     * @param user ログインユーザ情報
     * @return 登録用のフクキタル発注情報
     */
    private TFOrderEntity setValueForTFOrderEntity(final TFOrderEntity currentTFOrderEntity, final FukukitaruOrderModel fukukitaruOrderModel,
            final CustomLoginUser user) {
        // 確定ステータスの再セット(1(確定)をセット)
        fukukitaruOrderModel.setConfirmStatus(FukukitaruMasterConfirmStatusType.ORDER_CONFIRMED);
        // 連携ステータスの再セット(DB最新の登録値をセット)
        fukukitaruOrderModel.setLinkingStatus(currentTFOrderEntity.getLinkingStatus());
        fukukitaruOrderModel.setOrderSendAt(null);
        return fukukitaruOrderComponent.setValueForTFOrderEntity(fukukitaruOrderModel, user);
    }
}
