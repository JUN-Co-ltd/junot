package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.MakerReturnCompositeEntity;
import jp.co.jun.edi.model.MakerReturnSearchResultModel;

/**
 * メーカー返品一覧Repositoryのカスタムインターフェース.
 */
public interface MakerReturnCompositeRepositoryCustom {
    /**
     * TMakerReturnEntityを取得する.
     * @param searchCondition メーカー返品一覧検索条件
     * @param pageable Pageable
     * @return TDeliverySearchResultEntity
     */
    Page<MakerReturnCompositeEntity> findBySearchCondition(
            MakerReturnSearchResultModel searchCondition,
            Pageable pageable);

}
