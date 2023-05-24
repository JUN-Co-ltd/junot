package jp.co.jun.edi.service.maint.size;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSizeComponent;
import jp.co.jun.edi.model.maint.MaintSizeBulkResponseModel;
import jp.co.jun.edi.model.maint.MaintSizeBulkUpdateModel;
import jp.co.jun.edi.model.maint.code.MaintCountCUDCountModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericDeleteService;
import jp.co.jun.edi.service.parameter.MaintSizeBulkUpdateServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;
//PRD_0137 #10669 add start
/**
 * メンテナンスコード情報を削除するサービス.
 */
@Slf4j
@Service
public class MaintSizeBulkDeleteService extends GenericDeleteService<MaintSizeBulkUpdateServiceParameter, GetServiceResponse<MaintSizeBulkResponseModel>> {

    @Autowired
    private MaintSizeComponent maintSizeComponent;

    @Override
    protected GetServiceResponse<MaintSizeBulkResponseModel> execute(final MaintSizeBulkUpdateServiceParameter serviceParameter) {
    	log.info(LogStringUtil.of("execute")
                .message("Start processing of DeleteSizeMaster.")
                .build());
        final CustomLoginUser user = serviceParameter.getLoginUser();
        final MaintSizeBulkUpdateModel bulkUpdateModel = serviceParameter.getBulkUpdateModel();

        int deletedCnt = 0;
        // 1レコードずつ削除する
        for (Map<String, String> data : bulkUpdateModel.getItems()) {
            if (data.get("id") != null) {
                // 削除
                maintSizeComponent.deletedMaintCode(user.getUserId(), data);
                deletedCnt++;
            }
        }

        // レスポンス情報
        final MaintSizeBulkResponseModel model = new MaintSizeBulkResponseModel();
        model.setSuccess(new MaintCountCUDCountModel());
        model.getSuccess().setDeleted(deletedCnt);
        log.info(LogStringUtil.of("execute")
                .message("End processing of DeleteSizeMaster.")
                .build());
        return GetServiceResponse.<MaintSizeBulkResponseModel>builder().item(model).build();
    }

}
//PRD_0137 #10669 add end