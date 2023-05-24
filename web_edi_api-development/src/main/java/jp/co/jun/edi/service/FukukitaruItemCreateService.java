package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;

/**
 * TODO [未使用] フクキタル品番情報作成.
 */
@Service
public class FukukitaruItemCreateService
        extends GenericCreateService<CreateServiceParameter<FukukitaruItemModel>, CreateServiceResponse<FukukitaruItemModel>> {

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected CreateServiceResponse<FukukitaruItemModel> execute(final CreateServiceParameter<FukukitaruItemModel> serviceParameter) {
        final FukukitaruItemModel fukukitaruItemModel = serviceParameter.getItem();

        // フクキタル品番情報の登録
        fukukitaruItemComponent.save(fukukitaruItemModel);

        return CreateServiceResponse.<FukukitaruItemModel>builder().item(fukukitaruItemModel).build();
    }


}
