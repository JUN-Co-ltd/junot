package jp.co.jun.edi.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.TDelischeFileInfoEntity;
import jp.co.jun.edi.model.DelischeFileInfoModel;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.repository.TDelischeFileInfoRepository;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.DelischeFileStatusType;
import jp.co.jun.edi.util.ObjectMapperUtil;
/**
 * デリスケファイル情報を作成するサービス.
 */
@Service
public class DelischeFileInfoCreateService
    extends GenericFileCreateService<CreateServiceParameter<DelischeOrderSearchConditionModel>, CreateServiceResponse<DelischeFileInfoModel>> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TDelischeFileInfoRepository tDelischeFileInfoRepository;

    @Override
    protected CreateServiceResponse<DelischeFileInfoModel> execute(
            final CreateServiceParameter<DelischeOrderSearchConditionModel> serviceParameter) {

        final DelischeOrderSearchConditionModel delischeOrderSearchCondition = serviceParameter.getItem();

        final TDelischeFileInfoEntity delischeFileEntity = new TDelischeFileInfoEntity();
        delischeFileEntity.setStatus(DelischeFileStatusType.CREATING);
        delischeFileEntity.setSearchConditions(ObjectMapperUtil.writeValueAsString(delischeOrderSearchCondition));
        tDelischeFileInfoRepository.save(delischeFileEntity);

        final DelischeFileInfoModel delischeFile = new DelischeFileInfoModel();
        delischeFile.setId(delischeFileEntity.getId());
        return CreateServiceResponse.<DelischeFileInfoModel>builder().item(delischeFile).build();
    }
}
