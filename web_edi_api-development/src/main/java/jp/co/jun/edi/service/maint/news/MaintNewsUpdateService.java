package jp.co.jun.edi.service.maint.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintNewsComponent;
import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.service.GenericUpdateService;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;

/**
 * マスタメンテナンス用のお知らせ情報を更新するサービス.
 */
@Service
public class MaintNewsUpdateService
    extends GenericUpdateService<UpdateServiceParameter<MaintNewsModel>, UpdateServiceResponse<MaintNewsModel>> {

    @Autowired
    private MaintNewsComponent maintNewsComponent;

    @Autowired
    private TNewsRepository tNewsRepository;

    @Override
    protected UpdateServiceResponse<MaintNewsModel> execute(final UpdateServiceParameter<MaintNewsModel> serviceParameter) {
        final MaintNewsModel model = serviceParameter.getItem();

        final TNewsEntity entity = maintNewsComponent.getTNews(model.getId());

        // モデルからエンティティへコピー
        maintNewsComponent.copyModelToEntity(model, entity);

        // お知らせ情報を更新
        tNewsRepository.save(entity);

        return UpdateServiceResponse.<MaintNewsModel>builder().item(model).build();
    }
}
