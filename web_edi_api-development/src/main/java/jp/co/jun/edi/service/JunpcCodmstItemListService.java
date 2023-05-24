package jp.co.jun.edi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;

/**
 * アイテムマスタを取得するService.
 *
 */
@Service
public class JunpcCodmstItemListService extends GenericMCodmstListService {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());


        if (StringUtils.isAllEmpty(serviceParameter.getSearchCondition().getBrand(), serviceParameter.getSearchCondition().getItem())) {

            // ブランド・アイテムがすべてブランクの場合は、コードマスタに登録されているアイテムコードのみを返却する。
            return mCodmstRepository.findNoDuplicationItemCode(serviceParameter.getTblId().getValue(), pageRequest);
        }

        return mCodmstRepository.findByTblidAndCode1AndCode2OrderById(
                serviceParameter.getTblId().getValue(),
                serviceParameter.getSearchCondition().getBrand(),
                serviceParameter.getSearchCondition().getItem(),
                pageRequest);
    }

}
