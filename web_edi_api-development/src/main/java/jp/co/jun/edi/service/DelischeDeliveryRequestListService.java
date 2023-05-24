package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DelischeComponent;
import jp.co.jun.edi.model.DelischeDeliveryRequestSearchConditionModel;
import jp.co.jun.edi.model.VDelischeDeliveryRequestModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にデリスケ納品依頼情報を取得するサービス.
 */
@Service
public class DelischeDeliveryRequestListService extends GenericListService<ListServiceParameter<DelischeDeliveryRequestSearchConditionModel>,
ListServiceResponse<VDelischeDeliveryRequestModel>> {
    @Autowired
    private DelischeComponent delischeComponent;

    @Override
    protected ListServiceResponse<VDelischeDeliveryRequestModel>
    execute(final ListServiceParameter<DelischeDeliveryRequestSearchConditionModel> serviceParameter) {
        final List<VDelischeDeliveryRequestModel> vDelischeDeliveryRequestModelList
        = delischeComponent.listDelischeDeliveryRequest(serviceParameter.getSearchCondition());
        return ListServiceResponse.<VDelischeDeliveryRequestModel>builder().items(vDelischeDeliveryRequestModelList).build();
    }
}
