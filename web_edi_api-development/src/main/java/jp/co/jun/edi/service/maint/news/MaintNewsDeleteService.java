package jp.co.jun.edi.service.maint.news;

import java.math.BigInteger;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintNewsComponent;
import jp.co.jun.edi.entity.TNewsEntity;
import jp.co.jun.edi.repository.TNewsRepository;
import jp.co.jun.edi.repository.TNewsTagRepository;
import jp.co.jun.edi.service.GenericDeleteService;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.util.DateUtils;

/**
 * マスタメンテナンス用のお知らせ情報を削除するサービス.
 */
@Service
public class MaintNewsDeleteService
    extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {

    @Autowired
    private MaintNewsComponent maintNewsComponent;

    @Autowired
    private TNewsRepository tNewsRepository;

    @Autowired
    private TNewsTagRepository tNewsTagRepository;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        // お知らせ情報を取得し、データが存在しない場合は例外を投げる
        final TNewsEntity entity = maintNewsComponent.getTNews(serviceParameter.getId());

        // 削除日時を取得
        final Date deletedAt = DateUtils.createNow();

        // お知らせタグ情報を削除
        tNewsTagRepository.updateDelete(serviceParameter.getId(), deletedAt, serviceParameter.getLoginUser().getUserId());

        // 削除日時を設定
        entity.setDeletedAt(deletedAt);

        // 削除日時を更新
        tNewsRepository.save(entity);

        return DeleteServiceResponse.builder().build();
    }
}
