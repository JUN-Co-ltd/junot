package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.OrderCompositeEntity;
import jp.co.jun.edi.model.OrderSearchConditionModel;

/**
 * 発注情報Repositoryのカスタムインターフェース.
 */
public interface OrderCompositeRepositoryCustom {
    /**
     * 検索条件で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param supplierCode メーカーコード
     * @param pageable ページ情報
     * @return 結果
     */
    Page<OrderCompositeEntity> findBySearchCondition(
            OrderSearchConditionModel searchCondition,
            String supplierCode,
            Pageable pageable);
}
