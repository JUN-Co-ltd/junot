package jp.co.jun.edi.component.deliveryupsert;

import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.model.DeliverySkuModel;

/**
 * 納品SKU情報の訂正処理.
 */
@Component
public class DeliverySkuCorrectComponent extends GenericDeliverySkuUpsertComponent {

    @Override
    protected TDeliverySkuEntity generateEntityForInsert(final DeliverySkuModel deliverySkuForUpdate,
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> deliveryUpsertModel) {
        // 納品SKUのIDがない場合、INSERT
        final TDeliverySkuEntity insertDeliverySkuEntity = super.generateEntityForInsert(deliverySkuForUpdate, deliveryUpsertModel);

        // 納品依頼No再セット
        insertDeliverySkuEntity.setDeliveryRequestNumber(deliveryUpsertModel.getParentEntity().getDeliveryRequestNumber());

        return insertDeliverySkuEntity;
    }
}
