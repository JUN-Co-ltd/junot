//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.PurchaseRecordCompositeEntity;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;

/**
 * 仕入実績一覧情報Repositoryのカスタムインターフェース.
 */
public interface PurchaseRecordCompositeRepositoryCustom {

    /**
     * 検索結果で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable pageable
     * @return 検索結果
     */
    Page<PurchaseRecordCompositeEntity> findBySearchCondition(
            PurchaseRecordSearchConditionModel searchCondition,
            Pageable pageable);
}

//PRD_0133 #10181 add JFE end