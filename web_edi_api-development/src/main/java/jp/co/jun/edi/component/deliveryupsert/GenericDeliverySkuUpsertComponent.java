package jp.co.jun.edi.component.deliveryupsert;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品SKU情報の登録・更新処理.
 */
public abstract class GenericDeliverySkuUpsertComponent extends GenericDeliveryUpsertComponent<DeliverySkuModel, TDeliverySkuEntity, TDeliveryDetailEntity> {
    @Autowired
    private TDeliverySkuRepository deliverySkuRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected TDeliverySkuEntity generateEntityForInsert(final DeliverySkuModel deliverySkuForUpdate,
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // (納品SKUのIDがない場合のINSERT)
        return deliveryComponent.generateDeliverySkuForInsert(deliveryUpsertModel.getParentEntity(), deliverySkuForUpdate);
    }

    @Override
    protected TDeliverySkuEntity generateEntityForUpdate(final DeliverySkuModel deliverySkuForUpdate,
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // DBの最新の納品SKU情報を取得(取得できない場合はエラー)
        final TDeliverySkuEntity registeredDbDeliverySku = deliverySkuRepository.findByIdAndDeletedAtIsNull(deliverySkuForUpdate.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
        // 納品数量設定
        registeredDbDeliverySku.setDeliveryLot(deliverySkuForUpdate.getDeliveryLot());
        return registeredDbDeliverySku;
    }

    @Override
    protected BigInteger save(final TDeliverySkuEntity deliverySkuForUpsert) {
        final TDeliverySkuEntity resultDeliverySku = deliverySkuRepository.save(deliverySkuForUpsert);
        return resultDeliverySku.getId();
    }

    @Override
    protected boolean existsId(final DeliverySkuModel deliverySkuForUpdate) {
        return Objects.nonNull(deliverySkuForUpdate.getId());
    }

    @Override
    protected void setIdToModelForUpdate(final TDeliverySkuEntity resultTDeliverySkuEntity, final BigInteger id) {
        // 処理不要
    }

    @Override
    protected void upsertChild(final TDeliverySkuEntity entityForUpsert, final DeliverySkuModel modelForUpdate,
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // 処理不要
    }

    @Override
    protected void deleteExceptUpsertIds(final List<BigInteger> resultDeliverySkuIds,
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        final BigInteger parentId = deliveryUpsertModel.getParentEntity().getId();
        final BigInteger userId = deliveryUpsertModel.getLoginUser().getUserId();

        // 登録・更新した納品SKUのID以外のレコードを論理削除
        deliverySkuRepository.updateSkuDeletedAtByDeliveryDetailIdAndExclusionIds(parentId,
                resultDeliverySkuIds, userId);
    }
}
