package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * 納品情報を取得するサービス.
 */
@Service
public class DeliveryGetService
extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<DeliveryModel>> {

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected GetServiceResponse<DeliveryModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        final DeliveryModel deliveryModel = deliveryComponent.findDeliveryById(serviceParameter.getId());

        return GetServiceResponse.<DeliveryModel>builder().item(deliveryModel).build();
    }
}
