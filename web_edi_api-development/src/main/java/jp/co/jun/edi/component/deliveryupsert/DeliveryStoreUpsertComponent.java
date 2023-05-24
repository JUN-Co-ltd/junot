package jp.co.jun.edi.component.deliveryupsert;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryStoreEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryStoreModel;
import jp.co.jun.edi.model.DeliveryStoreSkuModel;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品得意先情報の登録・更新処理.
 */
@Component
public class DeliveryStoreUpsertComponent extends GenericDeliveryUpsertComponent<DeliveryStoreModel, TDeliveryStoreEntity, TDeliveryDetailEntity> {
    @Autowired
    private TDeliveryStoreRepository deliveryStoreRepository;

    @Autowired
    private DeliveryStoreSkuUpsertComponent deliveryStoreSkuUpsert;

    @Autowired
    private TDeliveryStoreSkuRepository deliveryStoreSkuRepository;

    @Override
    protected TDeliveryStoreEntity generateEntityForInsert(final DeliveryStoreModel deliveryStoreForUpdate,
            final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        final TDeliveryStoreEntity deliveryStoreEntity = new TDeliveryStoreEntity();

        // 店舗別配分率ID
        deliveryStoreEntity.setStoreDistributionRatioId(deliveryStoreForUpdate.getStoreDistributionRatioId());

        // 店舗別配分率区分
        deliveryStoreEntity.setStoreDistributionRatioType(deliveryStoreForUpdate.getStoreDistributionRatioType());

        // 店舗別配分率
        deliveryStoreEntity.setStoreDistributionRatio(deliveryStoreForUpdate.getStoreDistributionRatio());

        // 納品明細ID
        deliveryStoreEntity.setDeliveryDetailId(deliveryUpsertModel.getParentEntity().getId());

        // 店舗コード
        deliveryStoreEntity.setStoreCode(deliveryStoreForUpdate.getStoreCode());

        // 配分区分
        deliveryStoreEntity.setAllocationType(deliveryStoreForUpdate.getAllocationType());

        // 配分順
        deliveryStoreEntity.setDistributionSort(deliveryStoreForUpdate.getDistributionSort());

        return deliveryStoreEntity;
    }

    @Override
    protected TDeliveryStoreEntity generateEntityForUpdate(final DeliveryStoreModel deliveryStoreForUpdate,
            final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // DBの最新の納品得意先情報を取得(取得できない場合はエラー)
        final TDeliveryStoreEntity registeredDbDeliveryStore = deliveryStoreRepository.findByIdAndDeletedAtIsNull(deliveryStoreForUpdate.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 店舗別配分率ID
        registeredDbDeliveryStore.setStoreDistributionRatioId(deliveryStoreForUpdate.getStoreDistributionRatioId());

        // 店舗別配分率区分
        registeredDbDeliveryStore.setStoreDistributionRatioType(deliveryStoreForUpdate.getStoreDistributionRatioType());

        // 店舗別配分率
        registeredDbDeliveryStore.setStoreDistributionRatio(deliveryStoreForUpdate.getStoreDistributionRatio());

        return registeredDbDeliveryStore;
    }

    @Override
    protected BigInteger save(final TDeliveryStoreEntity deliveryStoreForUpsert) {
        final TDeliveryStoreEntity resultDeliveryStore = deliveryStoreRepository.save(deliveryStoreForUpsert);
        return resultDeliveryStore.getId();
    }

    @Override
    protected boolean existsId(final DeliveryStoreModel deliveryStoreForUpdate) {
        return Objects.nonNull(deliveryStoreForUpdate.getId());
    }

    @Override
    protected void setIdToModelForUpdate(final TDeliveryStoreEntity resultTDeliveryStoreEntity, final BigInteger id) {
        resultTDeliveryStoreEntity.setId(id);
    }

    @Override
    protected void upsertChild(final TDeliveryStoreEntity deliveryStoreEntityForUpsert, final DeliveryStoreModel deliveryStoreModelForUpdate,
            final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // 納品得意先SKUの登録・更新
        final DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity> storeSkuModel =
                new DeliveryUpsertModel<DeliveryStoreSkuModel, TDeliveryStoreEntity>();
        storeSkuModel.setModelForUpdateList(deliveryStoreModelForUpdate.getDeliveryStoreSkus());
        storeSkuModel.setLoginUser(deliveryUpsertModel.getLoginUser());
        storeSkuModel.setParentEntity(deliveryStoreEntityForUpsert);
        deliveryStoreSkuUpsert.upsert(storeSkuModel);
    }

    @Override
    protected void deleteExceptUpsertIds(final List<BigInteger> resultDeliveryStoreIds,
            final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        final BigInteger parentId = deliveryUpsertModel.getParentEntity().getId();
        final BigInteger userId = deliveryUpsertModel.getLoginUser().getUserId();

        // 登録・更新した納品得意先のID以外のレコードを論理削除
        deliveryStoreRepository.updateDeletedAtByDeliveryDetailIdAndExclusionIds(parentId, resultDeliveryStoreIds, userId);

        // 納品得意先内全削除時、納品得意先SKU論理削除
        deliveryStoreSkuRepository.updateDeletedAtByDeliveryDetailIdAndExclusionStoreIds(parentId, resultDeliveryStoreIds, userId);
    }
}
