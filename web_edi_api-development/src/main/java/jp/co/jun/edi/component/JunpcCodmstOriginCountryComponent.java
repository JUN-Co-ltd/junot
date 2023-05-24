package jp.co.jun.edi.component;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 原産国マスタ関連のコンポーネント.
 */
@Component
public class JunpcCodmstOriginCountryComponent extends GenericComponent {

    @Autowired
    private MCodmstRepository mCodmstRepository;

    /**
     * 指定された原産国コードが原産国マスタに存在するかチェックする.
     *
     * @param cooCode 原産国コード
     * @return true : 原産国マスタに指定された原産国コードが存在する, false : 原産国マスタに指定された原産国コードが存在しない
     */
    public boolean isExitsCooCode(final String cooCode) {
        List<MCodmstEntity> mCodmstEntityList = mCodmstRepository.findByTblidAndCode1OrderById(
                MCodmstTblIdType.ORIGIN_COUNTRY.getValue(),
                cooCode,
                PageRequest.of(0, 1)).getContent();

        return mCodmstEntityList.size() > 0;
    }
}
