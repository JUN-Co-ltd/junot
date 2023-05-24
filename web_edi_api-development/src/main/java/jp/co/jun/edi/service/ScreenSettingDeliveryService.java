package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ScreenSettingDeliveryComponent;
import jp.co.jun.edi.model.ScreenSettingDeliveryModel;
import jp.co.jun.edi.model.ScreenSettingDeliverySearchConditionModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 納品依頼画面構成情報を取得するService.
 */
@Service
public class ScreenSettingDeliveryService extends
    GenericListService<ListServiceParameter<ScreenSettingDeliverySearchConditionModel>, ListServiceResponse<ScreenSettingDeliveryModel>> {

    @Autowired
    private ScreenSettingDeliveryComponent screenSettingDeliverycomponent;

    @Override
    protected ListServiceResponse<ScreenSettingDeliveryModel> execute(
            final ListServiceParameter<ScreenSettingDeliverySearchConditionModel> serviceParameter) {

        // 納品依頼画面構成情報を取得
        final ScreenSettingDeliveryModel returnModel = screenSettingDeliverycomponent.execute(serviceParameter.getSearchCondition());

        // 戻り値
        final List<ScreenSettingDeliveryModel> items = new ArrayList<ScreenSettingDeliveryModel>();
        items.add(returnModel);
        return ListServiceResponse.<ScreenSettingDeliveryModel>builder().items(items).build();
    }
}
