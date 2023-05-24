package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.component.OrderComponent;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;


/**
 * 納品依頼関連テーブル登録処理.
 */
@Service
public class DeliveryCreateService
extends GenericCreateService<CreateServiceParameter<DeliveryModel>, CreateServiceResponse<DeliveryModel>> {

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private OrderComponent orderComponent;

    @Autowired
    private ItemComponent itemComponent;

    @Override
    protected CreateServiceResponse<DeliveryModel> execute(final CreateServiceParameter<DeliveryModel> serviceParameter) {
        final DeliveryModel deliveryModel = serviceParameter.getItem();

        // 品番情報取得。存在しない場合はエラー
        final ExtendedTItemEntity extendedTItemEntity = itemComponent.getExtendedTItem(deliveryModel.getPartNoId());

        // 発注情報取得。存在しない場合はエラー
        final ExtendedTOrderEntity extendedTOrderEntity = orderComponent.getExtendedTOrder(deliveryModel.getOrderId());

        // バリデーションチェック
        final ResultMessages rsltMsg = deliveryComponent.checkCommonValidate(deliveryModel, extendedTItemEntity, extendedTOrderEntity);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // 登録する納品依頼のリスト
        final List<TDeliveryEntity> registerDeliveries = new ArrayList<TDeliveryEntity>();

        final CustomLoginUser loginUser = serviceParameter.getLoginUser();

        // 納品依頼関連のテーブル登録
        if (Objects.nonNull(deliveryModel.getDeliveryDetails()) && !deliveryModel.getDeliveryDetails().isEmpty()) {
            deliveryComponent.insertIntoDeliveryAndChildRelationship(extendedTItemEntity, extendedTOrderEntity, loginUser, deliveryModel, registerDeliveries);
        }

        // メール送信
        deliveryComponent.sendDeliveryRequestRegistMails(extendedTItemEntity, extendedTOrderEntity, loginUser, registerDeliveries);

        // レスポンスに一番小さい納品依頼IDをセット
        final BigInteger smallestRegistedDeliveryId = deliveryComponent.getMinimumRegistedDeliveryId(registerDeliveries);
        deliveryModel.setId(smallestRegistedDeliveryId);

        return CreateServiceResponse.<DeliveryModel>builder().item(deliveryModel).build();
    }
}
