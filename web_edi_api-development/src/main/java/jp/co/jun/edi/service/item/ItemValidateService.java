package jp.co.jun.edi.service.item;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MItemComponent;
import jp.co.jun.edi.component.MessageComponent;
import jp.co.jun.edi.component.item.ItemValidateComponent;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ErrorDetailModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.ValidateModel;
import jp.co.jun.edi.service.GenericValidateService;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ValidateServiceResponse;

/**
 * 品番・商品検証Service.
 */
@Service
public class ItemValidateService
        extends GenericValidateService<ItemModel, ValidateModel> {
    @Autowired
    private ItemValidateComponent itemValidateComponent;

    @Autowired
    private MItemComponent mItemComponent;

    @Autowired
    private MessageComponent messageComponent;

    @Override
    protected ValidateServiceResponse<ValidateModel> execute(final ValidateServiceParameter<ItemModel> serviceParameter) {
        final List<ResultMessage> resultMessages = itemValidateComponent.getValidator()
                // 品番情報設定
                .item(serviceParameter.getItem())
                // マスタデータ設定
                .masterData(mItemComponent.getMasterData())
                // バリデーショングループリスト設定
                .validationGroups(serviceParameter.getValidationGroups())
                // 検証
                .validate();

        // ValidateModelにセット
        final List<ErrorDetailModel> errorDetailModels = messageComponent.toErrorDetails(resultMessages);
        final ValidateModel validateModel = new ValidateModel();
        validateModel.setErrors(errorDetailModels);

        return ValidateServiceResponse.<ValidateModel>builder().item(validateModel).build();
    }
}
