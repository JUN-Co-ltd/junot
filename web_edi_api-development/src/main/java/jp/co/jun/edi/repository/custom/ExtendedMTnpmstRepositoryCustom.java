package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.extended.ExtendedMTnpmstEntity;
import jp.co.jun.edi.model.ScreenSettingDeliverySearchConditionModel;

/**
 * ExtendedMTnpmstRepositoryのカスタムインターフェース.
 */
public interface ExtendedMTnpmstRepositoryCustom {
    /**
     * ExtendedMTemmstEntityを取得する.
     * @param searchCondition 検索条件
     * @return ExtendedMTemmstEntity
     */
    Page<ExtendedMTnpmstEntity> findBySpec(ScreenSettingDeliverySearchConditionModel searchCondition);
}
