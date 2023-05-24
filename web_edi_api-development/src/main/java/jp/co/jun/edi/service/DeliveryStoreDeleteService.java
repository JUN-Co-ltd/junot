package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.entity.TDeliveryDetailEntity;
import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TDeliveryStoreRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 納品得意先、納品得意先SKU削除処理.
 */
@Service
public class DeliveryStoreDeleteService
extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {
    @Autowired
    private TDeliveryRepository tDeliveryRepository;

    @Autowired
    private TDeliveryStoreRepository tDeliveryStoreRepository;

    @Autowired
    private TDeliveryStoreSkuRepository tDeliveryStoreSkuRepository;

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        final BigInteger deliveryId = serviceParameter.getId();

        validate(deliveryId, serviceParameter);

        final BigInteger userId = serviceParameter.getLoginUser().getUserId();
        tDeliveryStoreRepository.updateDeletedAtByDeliveryId(deliveryId, userId);
        tDeliveryStoreSkuRepository.updateDeletedAtByDeliveryId(deliveryId, userId);

        return DeleteServiceResponse.builder().build();
    }

    /**
     * @param deliveryId 納品ID
     * @param serviceParameter パラメータ
     */
    private void validate(
            final BigInteger deliveryId,
            final DeleteServiceParameter<BigInteger> serviceParameter) {
        final TDeliveryEntity tDeliveryEntity = tDeliveryRepository.findByIdAndDeletedAtIsNull(deliveryId).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_D_018)));

        // 承認済みの場合、バリデーションチェック
        if (ApprovalType.APPROVAL.getValue().equals(tDeliveryEntity.getDeliveryApproveStatus())) {
            deliveryComponent.judgeDeleteValidateAtApproval(serviceParameter, tDeliveryEntity);
        }

        // リクエストパラメータの納品IDに紐づく納品明細情報テーブルのレコードリストを取得。存在しない場合はエラー
        final List<TDeliveryDetailEntity> registeredDeliveryDetails = deliveryComponent.getTDeliveryDetailList(deliveryId);

        // 配分出荷指示済チェック
        final ResultMessages rsltMsg = ResultMessages.warning();
        deliveryComponent.checkShippingInstructed(registeredDeliveryDetails, rsltMsg);

        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }
    }
}
