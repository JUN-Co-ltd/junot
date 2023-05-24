package jp.co.jun.edi.repository.master.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.master.SireEntity;
import jp.co.jun.edi.model.maint.MaintSireSearchConditionModel;

/**
 * 取引先情報Repositoryのカスタムインターフェース.
 */
public interface SireRepositoryCustom {
    /**
     * 検索条件で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable ページ情報
     * @return 結果
     */
    Page<SireEntity> findBySearchCondition(
            MaintSireSearchConditionModel searchCondition,
            Pageable pageable);
}
