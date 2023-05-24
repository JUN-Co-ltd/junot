package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DeliveryComponent;
import jp.co.jun.edi.model.DeliveryModel;
import jp.co.jun.edi.model.DeliverySearchConditionModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基に納品情報を取得するサービス.
 */
@Service
public class DeliveryListService
extends GenericListService<ListServiceParameter<DeliverySearchConditionModel>, ListServiceResponse<DeliveryModel>> {

    @Autowired
    private DeliveryComponent deliveryComponent;

    @Override
    protected ListServiceResponse<DeliveryModel> execute(final ListServiceParameter<DeliverySearchConditionModel> serviceParameter) {
        // 検索用Limit、Offset
        final Integer limit = 100;
        final Integer offset = 0;

        final List<DeliveryModel> deliveryModelList = deliveryComponent.findDeliveriesBySpec(serviceParameter.getSearchCondition(), offset, limit);

        return ListServiceResponse.<DeliveryModel>builder().items(deliveryModelList).build();
    }
}
