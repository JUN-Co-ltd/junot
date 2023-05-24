package jp.co.jun.edi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;

/**
 * 発注生産システムのコードマスタから、ブランドコードを指定してゾーンを検索するService.
 */
@Service
public class JunpcCodmstZoneListService extends GenericMCodmstListService {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        return mCodmstRepository.findZoneByTblidAndCode1AheadLikeOrderByCode1(
                serviceParameter.getTblId().getValue(),
                serviceParameter.getSearchCondition().getBrand(),
                pageRequest);
    }
}
