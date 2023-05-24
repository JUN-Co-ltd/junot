package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.MakerReturnProductCompositeEntity;
import jp.co.jun.edi.model.MakerReturnProductSearchConditionModel;

/**
 * メーカー返品商品情報Repositoryのカスタムインターフェース.
 */
public interface MakerReturnProductCompositeRepositoryCustom {

    /**
     * 検索条件で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable ページ情報
     * @return 結果
     */
    Page<MakerReturnProductCompositeEntity> findBySearchCondition(
            MakerReturnProductSearchConditionModel searchCondition,
            Pageable pageable);
}
