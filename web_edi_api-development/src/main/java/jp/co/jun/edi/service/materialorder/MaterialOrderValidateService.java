package jp.co.jun.edi.service.materialorder;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MessageComponent;
import jp.co.jun.edi.component.materialorder.MaterialOrderValidateComponent;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.model.ErrorDetailModel;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.ValidateModel;
import jp.co.jun.edi.service.GenericValidateService;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ValidateServiceResponse;

/**
 * 資材発注登録検証Service.
 */
@Service
public class MaterialOrderValidateService extends GenericValidateService<FukukitaruOrderModel, ValidateModel> {
    /** ロケール. */
    private static final Locale LOCALE = Locale.JAPANESE;

    @Autowired
    private MaterialOrderValidateComponent materialOrderValidateComponent;

    @Autowired
    private MessageComponent messageComponent;

    @Override
    protected ValidateServiceResponse<ValidateModel> execute(final ValidateServiceParameter<FukukitaruOrderModel> serviceParameter) {
        final List<ResultMessage> resultMessages = materialOrderValidateComponent.getValidator()
                // 言語設定
                .locale(LOCALE)
                // バリデート対象データ設定
                .materialOrder(serviceParameter.getItem())
                // 検証実行
                .validate();

        // ValidateModelにセット
        final List<ErrorDetailModel> errorDetailModels = messageComponent.toErrorDetails(resultMessages);
        final ValidateModel validateModel = new ValidateModel();
        validateModel.setErrors(errorDetailModels);

        return ValidateServiceResponse.<ValidateModel>builder().item(validateModel).build();
    }

}
