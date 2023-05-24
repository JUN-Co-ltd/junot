package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.TDeliverySearchResultEntity;
import jp.co.jun.edi.model.DeliverySearchListConditionModel;

/**
 * 配分一覧Repositoryのカスタムインターフェース.
 */
public interface TDeliverySearchListRepositoryCustom {
    /**
     * TDeliverySearchResultEntityを取得する.
     * @param searchCondition 配分一覧検索条件
     * @return TDeliverySearchResultEntity
     */
    Page<TDeliverySearchResultEntity> findBySpec(DeliverySearchListConditionModel searchCondition);

    /**
     * TDeliverySearchResultEntityの件数を取得する.
     * @param searchCondition 配分一覧検索条件
     * @param fromPhraseSqlParam From句(任意)
     * @param wherePhraseSqlParam Where句(任意)
     * @return 件数
     */
    int countBySpec(DeliverySearchListConditionModel searchCondition, StringBuilder fromPhraseSqlParam,
            StringBuilder wherePhraseSqlParam);
}
