package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.entity.TFItemEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TFItemRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * TODO [未使用] フクキタル品番削除処理.
 */
@Service
public class FukukitaruItemDeleteService extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {

    @Autowired
    private TFItemRepository tfItemRepository;

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        // フクキタル発注情報を取得。取得できない(削除済み)場合は業務エラー
        final TFItemEntity tfItemEntity = tfItemRepository.findByFItemId(serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_FO_002)));

        fukukitaruItemComponent.delete(tfItemEntity);

        return DeleteServiceResponse.builder().build();
    }
}
