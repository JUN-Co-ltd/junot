package jp.co.jun.edi.service.bulkregist.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.bulkregist.BulkRegistItemComponent;
import jp.co.jun.edi.model.BulkRegistItemModel;
import jp.co.jun.edi.model.MultipartFileModel;
import jp.co.jun.edi.service.GenericValidateService;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ValidateServiceResponse;
import jp.co.jun.edi.type.RegistStatusType;

/**
 * 品番・商品一括登録検証Service.
 */
@Service
public class BulkRegistItemRegistValidateService
        extends GenericValidateService<MultipartFileModel, BulkRegistItemModel> {
    @Autowired
    private BulkRegistItemComponent bulkRegistItemComponent;

    @Override
    protected ValidateServiceResponse<BulkRegistItemModel> execute(final ValidateServiceParameter<MultipartFileModel> serviceParameter) {
        return ValidateServiceResponse.<BulkRegistItemModel>builder()
                .item(bulkRegistItemComponent.toItem(serviceParameter.getItem().getFile(), RegistStatusType.PART)).build();
    }
}
