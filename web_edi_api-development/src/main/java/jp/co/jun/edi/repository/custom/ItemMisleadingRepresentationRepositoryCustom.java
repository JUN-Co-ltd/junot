package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationSearchResultEntity;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchConditionModel;

/**
 * 優良誤認検査承認一覧Repositoryのカスタムインターフェース.
 */
public interface ItemMisleadingRepresentationRepositoryCustom {
    /**
     * ExtendedTMisleadingRepresentationViewEntityを取得する.
     * @param searchCondition 検索条件
     * @return ExtendedTMisleadingRepresentationViewEntity
     */
    Page<ExtendedItemMisleadingRepresentationSearchResultEntity> findBySpec(ItemMisleadingRepresentationSearchConditionModel searchCondition);
}
