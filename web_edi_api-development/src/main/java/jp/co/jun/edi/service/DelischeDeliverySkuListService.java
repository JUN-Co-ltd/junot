package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.DelischeComponent;
import jp.co.jun.edi.model.DelischeDeliverySkuSearchConditionModel;
import jp.co.jun.edi.model.VDelischeDeliverySkuModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 画面で指定された検索条件を基にデリスケ納品SKU情報を取得するサービス.
 */
@Service
public class DelischeDeliverySkuListService extends GenericListService<ListServiceParameter<DelischeDeliverySkuSearchConditionModel>,
ListServiceResponse<VDelischeDeliverySkuModel>> {
    @Autowired
    private DelischeComponent delischeComponent;

    @Override
    protected ListServiceResponse<VDelischeDeliverySkuModel>
    execute(final ListServiceParameter<DelischeDeliverySkuSearchConditionModel> serviceParameter) {
        final List<VDelischeDeliverySkuModel> vDelischeDeliverySkuModelList = delischeComponent.listDelischeDeliverySku(serviceParameter.getSearchCondition());
        return ListServiceResponse.<VDelischeDeliverySkuModel>builder().items(vDelischeDeliverySkuModelList).build();
    }
}
