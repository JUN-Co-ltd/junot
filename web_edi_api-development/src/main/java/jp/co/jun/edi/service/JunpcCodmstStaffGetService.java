package jp.co.jun.edi.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 社員情報を取得するサービス.
 */
@Service
public class JunpcCodmstStaffGetService extends GenericGetService<GetServiceParameter<String>, GetServiceResponse<JunpcCodmstModel>> {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected GetServiceResponse<JunpcCodmstModel> execute(final GetServiceParameter<String> serviceParameter) {
        final MCodmstEntity entity = mCodmstRepository.findByTblidAndCode1(
                MCodmstTblIdType.STAFF.getValue(),
                serviceParameter.getId()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        final JunpcCodmstModel model = new JunpcCodmstModel();
        BeanUtils.copyProperties(entity, model);

        return GetServiceResponse.<JunpcCodmstModel>builder().item(model).build();
    }


}
