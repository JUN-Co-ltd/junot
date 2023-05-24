package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.PurchaseCompositeEntity;
import jp.co.jun.edi.model.PurchaseSearchConditionModel;

/**
 * 仕入一覧情報Repositoryのカスタムインターフェース.
 */
public interface PurchaseCompositeRepositoryCustom {

    /**
     * 検索結果で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable pageable
     * @return 検索結果
     */
    Page<PurchaseCompositeEntity> findBySearchCondition(
            PurchaseSearchConditionModel searchCondition,
            Pageable pageable);
}

