package jp.co.jun.edi.service.maint.user;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintUserComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.model.maint.MaintUserModel;
import jp.co.jun.edi.service.GenericGetService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * マスタメンテナンス用のユーザ情報を取得するサービス.
 */
@Service
public class MaintUserGetService
    extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<MaintUserModel>> {

    @Autowired
    private MaintUserComponent maintUserComponent;

    @Override
    protected GetServiceResponse<MaintUserModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // ユーザ情報を取得し、データが存在しない場合は例外を投げる
        final MUserEntity entity = maintUserComponent.getMUser(serviceParameter.getId());

        final MaintUserModel model = new MaintUserModel();

        // エンティティからモデルへコピー
        maintUserComponent.copyEntityToModel(entity, model);

        return GetServiceResponse.<MaintUserModel>builder().item(model).build();
    }
}
