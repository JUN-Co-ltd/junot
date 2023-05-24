package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;

/**
 * TODO [未使用] フクキタル品番情報を更新するサービス.
 */
@Service
public class FukukitaruItemUpdateService
        extends GenericUpdateService<UpdateServiceParameter<FukukitaruItemModel>, UpdateServiceResponse<FukukitaruItemModel>> {

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected UpdateServiceResponse<FukukitaruItemModel> execute(final UpdateServiceParameter<FukukitaruItemModel> serviceParameter) {
        final FukukitaruItemModel fukukitaruItemModel = serviceParameter.getItem();

        fukukitaruItemComponent.update(fukukitaruItemModel);

        return UpdateServiceResponse.<FukukitaruItemModel>builder().item(fukukitaruItemModel).build();
    }




}
