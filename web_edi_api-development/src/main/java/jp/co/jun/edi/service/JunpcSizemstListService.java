package jp.co.jun.edi.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.JunpcSizemstListServiceDataComponent;
import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.model.JunpcSizmstModel;
import jp.co.jun.edi.model.JunpcSizmstSearchConditionModel;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 発注生産システムのサイズマスタからサイズを検索するService.
 */
@Service
public class JunpcSizemstListService extends GenericListService<ListServiceParameter<JunpcSizmstSearchConditionModel>, ListServiceResponse<JunpcSizmstModel>> {
    @Autowired
    private JunpcSizemstListServiceDataComponent junpcSizemstListServiceDataComponent;

    @Override
    protected ListServiceResponse<JunpcSizmstModel> execute(final ListServiceParameter<JunpcSizmstSearchConditionModel> serviceParameter) {
        final Page<MSizmstEntity> page = junpcSizemstListServiceDataComponent.find(serviceParameter.getSearchCondition());

        return ListServiceResponse.<JunpcSizmstModel>builder()
                .nextPage(page.hasNext())
                .items(page.stream().map(entity -> junpcSizemstListServiceDataComponent.toItem(entity)).collect(Collectors.toList()))
                .build();
    }


}
