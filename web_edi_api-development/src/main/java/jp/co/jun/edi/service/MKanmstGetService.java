package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.model.MKanmstModel;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;

/**
 * 発注生産システムの管理マスタから情報を取得するサービス.
 */
@Service
public class MKanmstGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<MKanmstModel>> {
    @Autowired
    private MKanmstComponent mKanmstComponent;

    @Override
    protected GetServiceResponse<MKanmstModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {
        // 管理マスタ情報を取得し、データが存在しない場合は例外を投げる
        final MKanmstEntity entity = mKanmstComponent.getMKanmstEntity();

        final MKanmstModel model = new MKanmstModel();

        // 管理マスタ情報のコピー
        BeanUtils.copyProperties(entity, model);

        return GetServiceResponse.<MKanmstModel>builder().item(model).build();
    }
}
