package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;

/**
 * 発注生産システムのコードマスタから、ブランドコードを指定して配分課を検索するService.
 */
@Service
public class JunpcCodmstAllocationListService extends GenericMCodmstListService {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final JunpcCodmstSearchConditionModel sc = serviceParameter.getSearchCondition();
        final PageRequest pageRequest = PageRequest.of(sc.getPage(), sc.getMaxResults());

        if ("1".equals(sc.getSearchType())) {
                return mCodmstRepository.findByTblidAndCode1List(
                        serviceParameter.getTblId().getValue(),
                        sc.getCode1s(),
                        pageRequest);
        }
        return mCodmstRepository.findAllocationByTblidAndCode1AheadLikeOrderById(
                serviceParameter.getTblId().getValue(),
                sc.getBrand(),
                pageRequest);
    }
}
