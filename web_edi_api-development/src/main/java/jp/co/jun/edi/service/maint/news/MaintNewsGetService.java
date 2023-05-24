package jp.co.jun.edi.service.maint.news;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintNewsComponent;
import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.service.GenericGetService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * マスタメンテナンス用のお知らせ情報を取得するサービス.
 */
@Service
public class MaintNewsGetService
    extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<MaintNewsModel>> {

    @Autowired
    private MaintNewsComponent maintNewsComponent;

    @Override
    protected GetServiceResponse<MaintNewsModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // お知らせ情報を取得し、データが存在しない場合は例外を投げる
        final TNewsEntity entity = maintNewsComponent.getTNews(serviceParameter.getId());

        final MaintNewsModel model = new MaintNewsModel();

        // エンティティからモデルへコピー
        maintNewsComponent.copyEntityToModel(entity, model);

        return GetServiceResponse.<MaintNewsModel>builder().item(model).build();
    }
}
