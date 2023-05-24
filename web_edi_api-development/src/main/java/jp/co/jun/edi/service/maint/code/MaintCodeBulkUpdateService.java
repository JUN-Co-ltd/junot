package jp.co.jun.edi.service.maint.code;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintCodeComponent;
import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.model.maint.code.MaintCountCUDCountModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkResponseModel;
import jp.co.jun.edi.model.maint.code.MaintCodeBulkUpdateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericCreateService;
import jp.co.jun.edi.service.parameter.MaintCodeBulkUpdateServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * メンテナンスコード情報を更新するサービス.
 */
@Service
public class MaintCodeBulkUpdateService extends GenericCreateService<MaintCodeBulkUpdateServiceParameter, GetServiceResponse<MaintCodeBulkResponseModel>> {

    @Autowired
    private MaintCodeComponent maintCodeComponent;

    @Override
    protected GetServiceResponse<MaintCodeBulkResponseModel> execute(final MaintCodeBulkUpdateServiceParameter serviceParameter) {
        final CustomLoginUser user = serviceParameter.getLoginUser();
        final MCodmstTblIdType tblId = serviceParameter.getTblId();
        final MaintCodeBulkUpdateModel bulkUpdateModel = serviceParameter.getBulkUpdateModel();

        // 画面構成情報取得
        final MScreenStructureEntity entity = maintCodeComponent.getMScreenStructureEntity(tblId);
        // 改訂日時の整合性チェック
        if (bulkUpdateModel.getRevisionedAt().compareTo(entity.getUpdatedAt()) != 0) {
            // エラー
            throw new BusinessException();
        }

        int updateCnt = 0;
        int registCnt = 0;
        // 1レコードずつ登録／更新する
        for (Map<String, String> data : bulkUpdateModel.getItems()) {
            if (data.get("id") == null) {
                // 登録
                maintCodeComponent.insertMaintCode(user.getUserId(), data, entity);
                registCnt++;
            } else {
                // 更新
                maintCodeComponent.updateMaintCode(user.getUserId(), data, entity);
                updateCnt++;
            }
        }

        // レスポンス情報
        final MaintCodeBulkResponseModel model = new MaintCodeBulkResponseModel();
        model.setSuccess(new MaintCountCUDCountModel());
        model.getSuccess().setUpdated(updateCnt);
        model.getSuccess().setRegisted(registCnt);

        return GetServiceResponse.<MaintCodeBulkResponseModel>builder().item(model).build();
    }

}
