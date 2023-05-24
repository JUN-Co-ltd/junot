package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.PurchaseComponent;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * 仕入情報を取得するサービス.
 */
@Service
public class PurchaseGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<PurchaseModel>> {

    @Autowired
    private TPurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseComponent purchaseComponent;

    @Override
    protected GetServiceResponse<PurchaseModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        final List<TPurchaseEntity> list = purchaseRepository.findByDeliveryId(serviceParameter.getId(), pageRequest).getContent();

        final PurchaseModel res = purchaseComponent.toPurchaseModel(list);

        return GetServiceResponse.<PurchaseModel>builder().item(res).build();
    }
}
