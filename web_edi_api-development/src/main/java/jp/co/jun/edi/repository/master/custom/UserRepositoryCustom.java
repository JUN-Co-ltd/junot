package jp.co.jun.edi.repository.master.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.entity.master.UserEntity;
import jp.co.jun.edi.model.maint.MaintUserSearchConditionModel;

/**
 * ユーザ情報Repositoryのカスタムインターフェース.
 */
public interface UserRepositoryCustom {
    /**
     * 検索条件で絞り込んだ結果を取得する.
     * @param searchCondition 検索条件
     * @param pageable ページ情報
     * @return 結果
     */
    Page<UserEntity> findBySearchCondition(
            MaintUserSearchConditionModel searchCondition,
            Pageable pageable);
}
