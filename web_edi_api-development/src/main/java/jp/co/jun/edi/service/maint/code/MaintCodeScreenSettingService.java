package jp.co.jun.edi.service.maint.code;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintCodeComponent;
import jp.co.jun.edi.model.maint.code.MaintCodeScreenSettingModel;
import jp.co.jun.edi.service.GenericGetService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * メンテナンスコードの画面構成を取得する.
 */
@Service
public class MaintCodeScreenSettingService extends GenericGetService<GetServiceParameter<MCodmstTblIdType>, GetServiceResponse<MaintCodeScreenSettingModel>> {

    @Autowired
    private MaintCodeComponent maintCodeComponent;

    @Override
    protected GetServiceResponse<MaintCodeScreenSettingModel> execute(final GetServiceParameter<MCodmstTblIdType> serviceParameter) {
        final MCodmstTblIdType tblid = serviceParameter.getId();

        final MaintCodeScreenSettingModel model = maintCodeComponent.generateMaintCodeScreenSetting(tblid);

        return GetServiceResponse.<MaintCodeScreenSettingModel>builder().item(model).build();
    }

}
