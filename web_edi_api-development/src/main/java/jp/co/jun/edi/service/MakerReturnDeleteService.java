package jp.co.jun.edi.service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.MakerReturnComponent;
import jp.co.jun.edi.component.model.MakerReturnKeyModel;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TMakerReturnRepository;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * メーカー返品削除処理.
 */
@Service
public class MakerReturnDeleteService extends GenericDeleteService<DeleteServiceParameter<MakerReturnKeyModel>, DeleteServiceResponse> {

    @Autowired
    private TMakerReturnRepository makerReturnRepository;

    @Autowired
    private MakerReturnComponent makerReturnComponent;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<MakerReturnKeyModel> serviceParameter) {
        final MakerReturnKeyModel key = serviceParameter.getId();

        final List<TMakerReturnEntity> dbMakerReturns = makerReturnRepository.findByVoucherNumberAndOrderId(key.getVoucherNumber(), key.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final ResultMessages rsltMsg = ResultMessages.warning();
        makerReturnComponent.checkValidateAtUpdate(dbMakerReturns, rsltMsg);
        if (rsltMsg.isNotEmpty()) {
            throw new BusinessException(rsltMsg);
        }

        // メーカー返品情報の論理削除
        final List<BigInteger> deleteIds = dbMakerReturns.stream()
                .map(TMakerReturnEntity::getId)
                .collect(Collectors.toList());
        makerReturnRepository.updateDeletedAtByIds(deleteIds, serviceParameter.getLoginUser().getUserId());

        return DeleteServiceResponse.builder().build();
    }
}
