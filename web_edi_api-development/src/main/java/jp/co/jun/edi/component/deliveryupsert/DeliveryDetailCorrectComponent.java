package jp.co.jun.edi.component.deliveryupsert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.DeliveryUpsertModel;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.model.DeliveryDetailModel;
import jp.co.jun.edi.model.DeliverySkuModel;
import jp.co.jun.edi.model.DeliveryStoreModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.LinkingStatusType;

/**
 * 納品明細情報の訂正処理.
 */
@Component
public class DeliveryDetailCorrectComponent extends GenericDeliveryDetailUpsertComponent {
    @Autowired
    private DeliverySkuCorrectComponent deliverySkuCorrect;

    @Autowired
    private DeliveryStoreUpsertComponent deliveryStoreUpsert;

    @Override
    protected TDeliveryDetailEntity generateEntityForInsert(final DeliveryDetailModel deliveryDetailForUpdate,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {

        final TDeliveryDetailEntity insertDeliveryDetailEntity = super.generateEntityForInsert(deliveryDetailForUpdate, deliveryUpsertModel);

        // 納品依頼No
        insertDeliveryDetailEntity.setDeliveryRequestNumber(deliveryDetailForUpdate.getDeliveryRequestNumber());
        // 一括仕入用納品No
        insertDeliveryDetailEntity.setBulkDeliveryNumber(null);

        /*
         * 1納品依頼に紐づく明細の下記カラムの値は全て同じであるため、
         * 新たに登録(INSERT)される納品明細も既存の値と同じにする
         * (訂正画面では製品で配分先が同一の新規の課を追加登録できる)
         */
        final TDeliveryDetailEntity registeredFirstDeliveryDetailEntity = deliveryUpsertModel.getRegisteredFirstDeliveryDetailEntity();
        // 納品No
        insertDeliveryDetailEntity.setDeliveryNumber(registeredFirstDeliveryDetailEntity.getDeliveryNumber());
        // 納品依頼日
        insertDeliveryDetailEntity.setDeliveryRequestAt(registeredFirstDeliveryDetailEntity.getDeliveryRequestAt());
        // 納品依頼回数
        insertDeliveryDetailEntity.setDeliveryCount(registeredFirstDeliveryDetailEntity.getDeliveryCount());

        return insertDeliveryDetailEntity;
    }

    @Override
    protected TDeliveryDetailEntity generateEntityForUpdate(final DeliveryDetailModel requestDeliveryDetailModel,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {
        final TDeliveryDetailEntity registeredDbDeliveryDetail = super.generateEntityForUpdate(requestDeliveryDetailModel, deliveryUpsertModel);

        // 納品依頼書発行フラグ
        registeredDbDeliveryDetail.setDeliverySheetOut(BooleanType.FALSE);
        // 連携ステータス
        registeredDbDeliveryDetail.setLinkingStatus(LinkingStatusType.TARGET);

        return registeredDbDeliveryDetail;
    }

    @Override
    protected void upsertChild(final TDeliveryDetailEntity deliveryDetailEntityForUpsert,
            final DeliveryDetailModel deliveryDetailModelForUpdate,
            final DeliveryUpsertModel<DeliveryDetailModel, TDeliveryEntity> deliveryUpsertModel) {
        final CustomLoginUser loginUser = deliveryUpsertModel.getLoginUser();

        // 納品得意先の訂正
        final DeliveryUpsertModel<DeliveryStoreModel, TDeliveryDetailEntity> storeModel = generateDeliveryStoreUpsertModel(
                deliveryDetailModelForUpdate, deliveryDetailEntityForUpsert, loginUser);
        deliveryStoreUpsert.upsert(storeModel);

        if (!deliveryUpsertModel.isFromStoreScreen()) {
            // 課別画面からの場合は、納品SKUの訂正
            final DeliveryUpsertModel<DeliverySkuModel, TDeliveryDetailEntity> skuModel = generateDeliverySkuUpsertModel(
                    deliveryDetailModelForUpdate, deliveryDetailEntityForUpsert, loginUser);
            deliverySkuCorrect.upsert(skuModel);
        }
    }
}
