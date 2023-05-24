package jp.co.jun.edi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;
import jp.co.jun.edi.type.MCodmstSearchType;

/**
 * 発注生産システムのコードマスタからカラーコードを検索するService.
 */
@Service
public class JunpcCodmstColorListService extends GenericMCodmstListService {
    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        if (StringUtils.isEmpty(serviceParameter.getSearchCondition().getSearchText())) {
            // 検索文字列が空の場合、全検索
            return mCodmstRepository.findByTblIdOrderByCode1(
                    serviceParameter.getTblId().getValue(),
                    pageRequest);
        }

        switch (MCodmstSearchType.findByValue(serviceParameter.getSearchCondition().getSearchType()).orElse(MCodmstSearchType.LIKE_CODE_OR_NAME)) {
        case LIKE_CODE_OR_NAME:
            // コード/名称 LIKE検索
            return mCodmstRepository.findByTblidAndCode1LikeOrItem2LikeOrderByCode1(
                    serviceParameter.getTblId().getValue(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        case LIKE_CODE:
            // コード LIKE検索
            return mCodmstRepository.findByTblidAndCode1LikeOrderByCode1(
                    serviceParameter.getTblId().getValue(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        case LIKE_NAME:
            // 名称 LIKE検索
            return mCodmstRepository.findByTblidAndItem2LikeOrderByCode1(
                    serviceParameter.getTblId().getValue(),
                    serviceParameter.getSearchCondition().getSearchText(),
                    pageRequest);
        default:
            return null;
        }
    }
}
