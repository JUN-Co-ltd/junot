package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ThresholdComponent;
import jp.co.jun.edi.model.ThresholdModel;
import jp.co.jun.edi.model.ThresholdSearchConditionModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 閾値マスタを検索するService.
 */
@Service
public class ThresholdListService extends GenericListService<ListServiceParameter<ThresholdSearchConditionModel>, ListServiceResponse<ThresholdModel>> {
    @Autowired
    private ThresholdComponent thresholdComponent;

    @Override
    protected ListServiceResponse<ThresholdModel> execute(final ListServiceParameter<ThresholdSearchConditionModel> serviceParameter) {

        final ThresholdSearchConditionModel searchCondition = serviceParameter.getSearchCondition();
        final List<ThresholdModel> thresholds = thresholdComponent.listThreshold(
                searchCondition.getBrandCode(),
                searchCondition.getItemCode());

        return ListServiceResponse.<ThresholdModel>builder().items(thresholds).build();
    }
}
