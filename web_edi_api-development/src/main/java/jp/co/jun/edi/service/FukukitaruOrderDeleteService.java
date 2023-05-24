package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruOrderComponent;
import jp.co.jun.edi.entity.TFOrderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TFOrderRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * TODO [未使用] フクキタル発注情報を削除するサービス.
 */
@Service
public class FukukitaruOrderDeleteService extends GenericDeleteService<DeleteServiceParameter<BigInteger>, DeleteServiceResponse> {

    @Autowired
    private FukukitaruOrderComponent fukukitaruOrderComponent;
    @Autowired
    private TFOrderRepository tfOrderRepository;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<BigInteger> serviceParameter) {
        // フクキタル発注情報を取得。取得できない(削除済み)場合は業務エラー
        final TFOrderEntity tfOrderEntity = tfOrderRepository.findById(serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_FO_002)));

        fukukitaruOrderComponent.delete(tfOrderEntity);

        return DeleteServiceResponse.builder().build();
    }
}
