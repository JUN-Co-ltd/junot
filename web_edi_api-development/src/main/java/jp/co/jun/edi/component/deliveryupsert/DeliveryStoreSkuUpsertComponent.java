package jp.co.jun.edi.component.deliveryupsert;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryStoreSkuModel;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品得意先SKU情報の登録・更新処理.
 */
@Component
public class DeliveryStoreSkuUpsertComponent extends GenericDeliveryUpsertComponent<DeliveryStoreSkuModel, TDeliveryStoreSkuEntity, TDeliveryStoreEntity> {
    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Override
    protected TDeliveryStoreSkuEntity generateEntityForInsert(final DeliveryStoreSkuModel deliveryStoreSkuForUpdate,
            final DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity> deliveryUpsertModel) {
        final TDeliveryStoreSkuEntity deliveryStoreSkuEntity = new TDeliveryStoreSkuEntity();

        // サイズ
        deliveryStoreSkuEntity.setSize(deliveryStoreSkuForUpdate.getSize());
        // カラー
        deliveryStoreSkuEntity.setColorCode(deliveryStoreSkuForUpdate.getColorCode());
        // 納品数量
        deliveryStoreSkuEntity.setDeliveryLot(deliveryStoreSkuForUpdate.getDeliveryLot());

        // 納品得意先ID
        deliveryStoreSkuEntity.setDeliveryStoreId(deliveryUpsertModel.getParentEntity().getId());

        return deliveryStoreSkuEntity;
    }

    @Override
    protected TDeliveryStoreSkuEntity generateEntityForUpdate(final DeliveryStoreSkuModel deliveryStoreSkuForUpdate,
            final DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity> deliveryUpsertModel) {
        // DBの最新の納品得意先SKU情報を取得(取得できない場合はエラー)
        final TDeliveryStoreSkuEntity registeredDbDeliveryStoreSku = deliveryStoreSkuRepository
                .findByIdAndDeletedAtIsNull(deliveryStoreSkuForUpdate.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // サイズ
        registeredDbDeliveryStoreSku.setSize(deliveryStoreSkuForUpdate.getSize());
        // カラー
        registeredDbDeliveryStoreSku.setColorCode(deliveryStoreSkuForUpdate.getColorCode());
        // 納品数量
        registeredDbDeliveryStoreSku.setDeliveryLot(deliveryStoreSkuForUpdate.getDeliveryLot());

        return registeredDbDeliveryStoreSku;
    }

    @Override
    protected BigInteger save(final TDeliveryStoreSkuEntity deliveryStoreSkuForUpsert) {
        final TDeliveryStoreSkuEntity resultDeliveryStoreSku = deliveryStoreSkuRepository.save(deliveryStoreSkuForUpsert);
        return resultDeliveryStoreSku.getId();
    }

    @Override
    protected boolean existsId(final DeliveryStoreSkuModel deliveryStoreSkuForUpdate) {
        return Objects.nonNull(deliveryStoreSkuForUpdate.getId());
    }

    @Override
    protected void setIdToModelForUpdate(final TDeliveryStoreSkuEntity resultTDeliveryStoreSkuEntity, final BigInteger id) {
        // 処理不要
    }

    @Override
    protected void upsertChild(final TDeliveryStoreSkuEntity entityForUpsert, final DeliveryStoreSkuModel modelForUpdate,
            final DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity> deliveryUpsertModel) {
        // 処理不要
    }

    @Override
    protected void deleteExceptUpsertIds(final List<BigInteger> resultDeliveryStoreSkuIds,
            final DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity> deliveryUpsertModel) {
        final BigInteger parentId = deliveryUpsertModel.getParentEntity().getId();
        final BigInteger loginUserId = deliveryUpsertModel.getLoginUser().getUserId();

        // 登録・更新した納品得意先SKUのID以外のレコードを論理削除
        deliveryStoreSkuRepository.updateDeletedAtByDeliveryStoreIdAndExclusionIds(
                parentId, resultDeliveryStoreSkuIds, loginUserId);
    }
}
