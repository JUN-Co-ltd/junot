package jp.co.jun.edi.service.maint.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintUserComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.maint.MaintUserModel;
import jp.co.jun.edi.service.GenericCreateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;

/**
 * マスタメンテナンス用のユーザ情報を作成するサービス.
 */
@Service
public class MaintUserCreateService
    extends GenericCreateService<CreateServiceParameter<MaintUserModel>, CreateServiceResponse<MaintUserModel>> {

    @Autowired
    private MaintUserComponent maintUserComponent;

    @Override
    protected CreateServiceResponse<MaintUserModel> execute(final CreateServiceParameter<MaintUserModel> serviceParameter) {
        final MaintUserModel model = serviceParameter.getItem();

        final MUserEntity entity = new MUserEntity();

        // モデルからエンティティへコピー
        maintUserComponent.copyModelToEntity(model, entity);

        // 固定値の設定
        entity.setSystemManaged(false);

        // ユーザ情報を保存
        maintUserComponent.save(entity);

        // 登録されたIDを設定
        model.setId(entity.getId());

        return CreateServiceResponse.<MaintUserModel>builder().item(model).build();
    }
}
