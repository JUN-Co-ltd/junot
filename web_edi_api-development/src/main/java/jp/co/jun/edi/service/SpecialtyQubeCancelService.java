package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.component.SpecialtyQubeComponent;
import jp.co.jun.edi.component.model.SpecialtyQubeCancelResponseXmlModel;
import jp.co.jun.edi.component.model.SpecialtyQubeRequestXmlModel;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.SpecialtyQubeCancelResponseModel;
import jp.co.jun.edi.model.SpecialtyQubeRequestModel;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.SpecialtyQubeCancelStatusType;

/**
 * 店別配分キャンセルAPI実行処理.
 */
@Service
public class SpecialtyQubeCancelService
extends GenericUpdateService<UpdateServiceParameter<SpecialtyQubeRequestModel>, UpdateServiceResponse<SpecialtyQubeCancelResponseModel>> {

    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Autowired
    private SpecialtyQubeComponent sqComponent;

    @Override
    protected UpdateServiceResponse<SpecialtyQubeCancelResponseModel> execute(final UpdateServiceParameter<SpecialtyQubeRequestModel> serviceParameter) {
        final BigInteger deliveryId = serviceParameter.getItem().getDeliveryId();
        final BigInteger loginUserId = serviceParameter.getLoginUser().getUserId();
        // 納品情報を取得し、データが存在しない場合は例外を投げる
        final TDeliveryEntity deliveryEntity = tDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_015)));

        // ログインユーザー以外のユーザーがSQロックユーザーであればSQに投げずにロックユーザーアカウントを返す
        final BigInteger sqLockUserId = deliveryEntity.getSqLockUserId();
        if (!Objects.isNull(sqLockUserId) && !sqLockUserId.equals(loginUserId)) {
            final SpecialtyQubeCancelResponseModel res = new SpecialtyQubeCancelResponseModel();
            res.setSqLockUserId(sqLockUserId);
            return UpdateServiceResponse.<SpecialtyQubeCancelResponseModel>builder().item(res).build();

        }

        // ログインユーザがSQロックユーザの場合は訂正可とする。
        if (!Objects.isNull(sqLockUserId) && sqLockUserId.equals(loginUserId)) {
            final SpecialtyQubeCancelResponseModel res = new SpecialtyQubeCancelResponseModel();
            res.setStatus(SpecialtyQubeCancelStatusType.CANCEL_OK);
            return UpdateServiceResponse.<SpecialtyQubeCancelResponseModel>builder().item(res).build();
        }

        // 変更可否チェック.不可の場合業務エラー
        deliveryComponent.checkChangeDelivery(deliveryEntity);

        // SQキャンセル
        final SpecialtyQubeCancelResponseModel res = new SpecialtyQubeCancelResponseModel();
        final SpecialtyQubeRequestXmlModel reqXmlModel = new SpecialtyQubeRequestXmlModel();
        reqXmlModel.setOrderNum(deliveryEntity.getOrderNumber());
        reqXmlModel.setOrderCount(String.format("%02d", deliveryEntity.getDeliveryCount()));
        final SpecialtyQubeCancelResponseXmlModel result = sqComponent.executeCancel(reqXmlModel);
        if (!Objects.isNull(result)) {
            res.setProcesstime(result.getProcesstime());
            res.setStatus(result.getStatus());
            res.setErrorList(result.getErrorList());
            res.setSqLockUserId(sqLockUserId);

            // キャンセルOKの場合はロックする
            if (result.getStatus() == SpecialtyQubeCancelStatusType.CANCEL_OK) {
                tDeliveryRepository.lockSq(BooleanType.TRUE, deliveryId, loginUserId);
            }
        }

        return UpdateServiceResponse.<SpecialtyQubeCancelResponseModel>builder().item(res).build();
    }

}
