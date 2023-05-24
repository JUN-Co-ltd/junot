//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.repository.custom;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.PurchaseRecordCsvEntity;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;

/**
 * 仕入実績一覧情報Repositoryのカスタムインターフェース.
 */
public interface PurchaseRecordCsvCompositeRepositoryCustom {

    /**
     * 検索結果で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable pageable
     * @return 検索結果
     */
    Page<PurchaseRecordCsvEntity> findBySearchCondition(
            PurchaseRecordSearchConditionModel searchCondition,
            Pageable pageable);


    /**
     * 検索結果で絞り込んだ結果を取得する(PDF用).
     * @param searchCondition 検索条件
     * @param pageable pageable
     * @return 検索結果
     */
    List<PurchaseRecordCsvEntity> findPDFDetailBySearchCondition(
            PurchaseRecordSearchConditionModel searchCondition,
            Pageable pageable);
}

//PRD_0133 #10181 add JFE end