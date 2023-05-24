package jp.co.jun.edi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 発注生産システムのコードマスタからVoi区分を検索するService.
 */
@Service
public class JunpcCodmstVoiSectionListService extends GenericMCodmstListService {
    private static final String FLAG_ON = "1";

    @Autowired
    private MCodmstRepository mCodmstRepository;

    @Override
    protected Page<MCodmstEntity> find(final JunpcCodmstListServiceParameter serviceParameter) {
        final PageRequest pageRequest = PageRequest.of(
                serviceParameter.getSearchCondition().getPage(),
                serviceParameter.getSearchCondition().getMaxResults());

        // コードマスタ（Voi展開ブランド）に対象ブランドのレコードが入っているか確認する
        List<MCodmstEntity> voiBrandList = mCodmstRepository.findByTblidAndCode1AndItem1OrderById(
                MCodmstTblIdType.VOI_BRAND.getValue(),
                serviceParameter.getSearchCondition().getBrand(),
                FLAG_ON,
                PageRequest.of(0, 1)).getContent();

        // レコードがある場合はVoi区分を検索して返却
        if (voiBrandList.size() > 0) {
            return mCodmstRepository.findByTblIdOrderByCode1(
                    serviceParameter.getTblId().getValue(),
                    pageRequest);
        }
        // レコードがない場合はnullを返却
        return null;
    }
}
