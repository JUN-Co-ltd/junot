package jp.co.jun.edi.service.maint.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintUserComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.maint.MaintUserModel;
import jp.co.jun.edi.service.GenericUpdateService;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;

/**
 * マスタメンテナンス用のユーザ情報を更新するサービス.
 */
@Service
public class MaintUserUpdateService
    extends GenericUpdateService<UpdateServiceParameter<MaintUserModel>, UpdateServiceResponse<MaintUserModel>> {

    @Autowired
    private MaintUserComponent maintUserComponent;

    @Override
    protected UpdateServiceResponse<MaintUserModel> execute(final UpdateServiceParameter<MaintUserModel> serviceParameter) {
        final MaintUserModel model = serviceParameter.getItem();

        // ユーザ情報を取得し、データが存在しない場合は例外を投げる
        final MUserEntity entity = maintUserComponent.getMUser(model.getId());

        // モデルからエンティティへコピー
        maintUserComponent.copyModelToEntity(model, entity);

        // ユーザ情報を保存
        maintUserComponent.save(entity);

        return UpdateServiceResponse.<MaintUserModel>builder().item(model).build();
    }
}
