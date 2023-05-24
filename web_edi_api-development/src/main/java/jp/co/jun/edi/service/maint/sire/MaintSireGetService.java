package jp.co.jun.edi.service.maint.sire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSireComponent;
import jp.co.jun.edi.component.model.MaintSireKeyModel;
import jp.co.jun.edi.entity.MSireEntity;
import jp.co.jun.edi.model.maint.MaintSireModel;
import jp.co.jun.edi.service.GenericGetService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * マスタメンテナンス用の仕入先情報を取得するサービス.
 */
@Service
public class MaintSireGetService
    extends GenericGetService<GetServiceParameter<MaintSireKeyModel>, GetServiceResponse<MaintSireModel>> {

    @Autowired
    private MaintSireComponent maintSireComponent;

    @Override
    protected GetServiceResponse<MaintSireModel> execute(final GetServiceParameter<MaintSireKeyModel> serviceParameter) {
        // 仕入先情報を取得し、データが存在しない場合は例外を投げる
        final MSireEntity entity = maintSireComponent.getMSire(serviceParameter.getId());

        final MaintSireModel model = new MaintSireModel();

        // エンティティからモデルへコピー
        maintSireComponent.copyMSireEntityEntityToModel(entity, model);

        return GetServiceResponse.<MaintSireModel>builder().item(model).build();
    }

}
