package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 仕入情報を作成するサービス.
 */
@Service
public class PurchaseCreateService
extends GenericCreateService<CreateServiceParameter<PurchaseModel>, CreateServiceResponse<PurchaseModel>> {

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseComponent purchaseComponent;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected CreateServiceResponse<PurchaseModel> execute(final CreateServiceParameter<PurchaseModel> serviceParameter) {
        final PurchaseModel purchaseRequest = serviceParameter.getItem();

        checkValidate(purchaseRequest);

        final List<TPurchaseEntity> entities = purchaseComponent.toPurchaseEntitiesForUpsert(purchaseRequest);

        // 仕入情報の登録
        purchaseRepository.saveAll(entities);

        // 納品明細更新
        deliveryComponent.updateDeliveryDetailArrivalPlace(purchaseRequest, serviceParameter.getLoginUser().getUserId());

        // レスポンス作成
        final PurchaseModel res = purchaseComponent.toPurchaseModel(entities);

        return CreateServiceResponse.<PurchaseModel>builder().item(res).build();
    }

    /**
     * バリデーションチェック.
     * @param purchaseRequest リクエストの仕入情報
     */
    private void checkValidate(final PurchaseModel purchaseRequest) {
        final DeliveryModel delivery = deliveryComponent.findDeliveryById(purchaseRequest.getDeliveryId());
        final ResultMessages rsltMsg = purchaseComponent.checkValidate(purchaseRequest, delivery);

        // 直送の場合、エラー
        if (delivery.getDeliveryDetails().stream().anyMatch(dd -> CarryType.DIRECT == dd.getCarryType())) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_PC_008));
        }

        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
    }


}
