package jp.co.jun.edi.service.maint.user;

import java.math.BigInteger;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintUserComponent;
import jp.co.jun.edi.entity.MUserEntity;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.service.GenericDeleteService;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.util.DateUtils;

/**
 * マスタメンテナンス用のユーザ情報を削除するサービス.
 */
@Service
public class MaintUserDeleteService
    extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {

    @Autowired
    private MaintUserComponent maintUserComponent;

    @Autowired
    private MUserRepository tUserRepository;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        // ユーザ情報を取得し、データが存在しない場合は例外を投げる
        final MUserEntity entity = maintUserComponent.getMUser(serviceParameter.getId());

        // 削除日時を取得
        final Date deletedAt = DateUtils.createNow();

        // 削除日時を設定
        entity.setDeletedAt(deletedAt);

        // 削除日時を更新
        tUserRepository.save(entity);

        return DeleteServiceResponse.builder().build();
    }
}
