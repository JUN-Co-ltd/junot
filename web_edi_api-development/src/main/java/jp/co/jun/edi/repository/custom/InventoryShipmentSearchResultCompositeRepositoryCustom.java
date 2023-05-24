package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.extended.ExtendedTInventoryShipmentSearchResultEntity;
import jp.co.jun.edi.model.InventoryShipmentSearchConditionModel;

/**
 * InventoryShipmentSearchResultCompositeRepositoryのカスタムインターフェース.
 */
public interface InventoryShipmentSearchResultCompositeRepositoryCustom {
    /**
     * ExtendedMTemmstEntityを取得する.
     * @param searchCondition 検索条件
     * @param pageable pageable
     * @return 検索結果
     */
    Page<ExtendedTInventoryShipmentSearchResultEntity>
    findBySearchCondition(InventoryShipmentSearchConditionModel searchCondition,
            Pageable pageable);
}
