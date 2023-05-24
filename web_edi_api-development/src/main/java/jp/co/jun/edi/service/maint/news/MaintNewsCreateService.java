package jp.co.jun.edi.service.maint.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintNewsComponent;
import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.service.GenericCreateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;

/**
 * マスタメンテナンス用のお知らせ情報を作成するサービス.
 */
@Service
public class MaintNewsCreateService
    extends GenericCreateService<CreateServiceParameter<MaintNewsModel>, CreateServiceResponse<MaintNewsModel>> {

    @Autowired
    private MaintNewsComponent maintNewsComponent;

    @Autowired
    private TNewsRepository tNewsRepository;

    @Override
    protected CreateServiceResponse<MaintNewsModel> execute(final CreateServiceParameter<MaintNewsModel> serviceParameter) {
        final MaintNewsModel model = serviceParameter.getItem();

        final TNewsEntity entity = new TNewsEntity();

        // モデルからエンティティへコピー
        maintNewsComponent.copyModelToEntity(model, entity);

        // お知らせ情報を登録
        tNewsRepository.save(entity);

        model.setId(entity.getId());

        return CreateServiceResponse.<MaintNewsModel>builder().item(model).build();
    }
}
