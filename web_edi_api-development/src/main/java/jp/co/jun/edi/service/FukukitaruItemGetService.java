package jp.co.jun.edi.service;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruItemComponent;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * TODO [未使用] フクキタル品番情報を取得するサービス.
 *
 */
@Service
public class FukukitaruItemGetService extends GenericGetService<GetServiceParameter<BigInteger>, GetServiceResponse<FukukitaruItemModel>> {

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    @Autowired
    private FukukitaruItemComponent fukukitaruItemComponent;

    @Override
    protected GetServiceResponse<FukukitaruItemModel> execute(final GetServiceParameter<BigInteger> serviceParameter) {

        // フクキタル品番情報を取得し、データが存在しない場合は例外をスローする
        final FukukitaruItemModel fkItemModel = fukukitaruItemComponent.generatedFukukitaruItemModelSearchFItemId(serviceParameter.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // 品番情報を取得し、データが存在しない場合は例外をスローする
        extendedTItemRepository.findById(fkItemModel.getPartNoId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        return GetServiceResponse.<FukukitaruItemModel>builder().item(fkItemModel).build();

    }

}
