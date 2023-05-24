package jp.co.jun.edi.repository.custom;

import org.springframework.data.domain.Page;

import jp.co.jun.edi.entity.VDelischeOrderEntity;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;

/**
 * デリスケ発注Repositoryのカスタムインターフェース.
 */
public interface VDelischeOrderRepositoryCustom {
    /**
     * VDelischeDeliveryRequestEntityを取得する.
     * @param searchCondition デリスケ発注検索条件
     * @return VDelischeOrderEntity
     */
    Page<VDelischeOrderEntity> findBySpec(DelischeOrderSearchConditionModel searchCondition);

    /**
     * VDelischeDeliveryRequestEntityの件数を取得する.
     * @param searchCondition デリスケ発注検索条件
     * @param fromPhraseSqlParam From句(任意)
     * @param wherePhraseSqlParam Where句(任意)
     * @return 件数
     */
    int countBySpec(DelischeOrderSearchConditionModel searchCondition, StringBuilder fromPhraseSqlParam,
            StringBuilder wherePhraseSqlParam);
}
