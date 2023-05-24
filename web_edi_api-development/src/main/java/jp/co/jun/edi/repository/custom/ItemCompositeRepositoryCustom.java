package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.extended.ExtendedTItemListEntity;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;

/**
 * 品番情報Repositoryのカスタムインターフェース.
 */
public interface ItemCompositeRepositoryCustom {
    /**
     * 検索条件で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param loginUser ログインユーザ情報
     * @param pageable ページ情報
     * @return 結果
     */
    Page<ExtendedTItemListEntity> findBySpec(
            ItemSearchConditionModel searchCondition,
            CustomLoginUser loginUser,
            Pageable pageable);

}
