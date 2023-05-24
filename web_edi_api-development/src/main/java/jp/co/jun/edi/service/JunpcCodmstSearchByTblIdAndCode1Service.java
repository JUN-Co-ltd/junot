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
 * 発注生産システムのコードマスタを検索するService.
 */
@Service
public class JunpcCodmstSearchByTblIdAndCode1Service extends GenericMCodmstListService {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final JunpcCodmstSearchConditionModel searchCondition = serviceParameter.getSearchCondition();
        final PageRequest pageRequest = PageRequest.of(
                searchCondition.getPage(),
                searchCondition.getMaxResults());

        return mCodmstRepository.findByTblIdAndCode1(
                serviceParameter.getTblId().getValue(),
                searchCondition.getCode1(),
                pageRequest);
    }
}
