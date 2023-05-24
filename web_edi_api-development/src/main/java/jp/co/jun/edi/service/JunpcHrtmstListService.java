package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.JunpcHrtmstComponent;
import jp.co.jun.edi.model.JunpcHrtmstModel;
import jp.co.jun.edi.model.JunpcHrtmstSearchConditionModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムの配分率マスタから配分率を検索するService.
 */
@Service
public class JunpcHrtmstListService extends GenericListService<ListServiceParameter<JunpcHrtmstSearchConditionModel>, ListServiceResponse<JunpcHrtmstModel>> {

    @Autowired
    private JunpcHrtmstComponent junpcHrtmstComponent;

    @Override
    protected ListServiceResponse<JunpcHrtmstModel> execute(final ListServiceParameter<JunpcHrtmstSearchConditionModel> serviceParameter) {

        final List<JunpcHrtmstModel> hrtmst = junpcHrtmstComponent.findHrtmst(serviceParameter.getSearchCondition().getBrandCode(),
                serviceParameter.getSearchCondition().getItemCode(),
                serviceParameter.getSearchCondition().getSeason());

        return ListServiceResponse.<JunpcHrtmstModel>builder().items(hrtmst).build();
    }


}
