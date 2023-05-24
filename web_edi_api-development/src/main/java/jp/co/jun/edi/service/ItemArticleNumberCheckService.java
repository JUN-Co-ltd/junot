package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.ItemArticleNumberValidateComponent;
import jp.co.jun.edi.component.MessageComponent;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.ValidateModel;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;

/**
 * JAN/UPCバリデーションチェック処理.
 */
@Service
public class ItemArticleNumberCheckService extends GenericUpdateService<UpdateServiceParameter<ItemModel>, UpdateServiceResponse<ValidateModel>> {

    @Autowired
    private ItemArticleNumberValidateComponent itemArticleNumberValidateComponent;

    @Autowired
    private MessageComponent messageComponent;

    @Override
    protected UpdateServiceResponse<ValidateModel> execute(final UpdateServiceParameter<ItemModel> serviceParameter) {
        // 戻り値
        final ValidateModel validatetModel = new ValidateModel();

        validatetModel.setErrors(messageComponent.toErrorDetails(itemArticleNumberValidateComponent.validateArticleNumber(serviceParameter.getItem())));

        return UpdateServiceResponse.<ValidateModel>builder().item(validatetModel).build();
    }
}
