
package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedMFInputAssistSetEntity;

/**
 *
 * ExtendedTItemListRepository.
 *
 */
@Repository
public interface ExtendedMFInputAssistSetRepository
        extends JpaRepository<ExtendedMFInputAssistSetEntity, BigInteger>, JpaSpecificationExecutor<ExtendedMFInputAssistSetEntity> {

    /**
     * 入力補助セットIDから 入力補助セット情報 を検索する.
     *
     * @param inputAssistSetId 入力補助セットID
     * @param pageable Pageable
     * @return 拡張入力補助セット情報
     */
    @Query(value = " SELECT"
            + "    t1.id AS input_assist_set_id"
            + "   ,t2.id AS input_assist_set_detail_id"
            + "   ,t1.set_name"
            + "   ,t2.material_type"
            + "   ,t2.material_id_list"
            + " FROM m_f_input_assist_set t1"
            + " LEFT JOIN m_f_input_assist_set_detail t2"
            + "   ON t1.id = t2.assist_set_id"
            + "   AND t2.deleted_at IS NULL"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.id = :inputAssistSetId"
            + " ORDER BY t1.id ASC, t1.sort_order ASC", nativeQuery = true)
    Page<ExtendedMFInputAssistSetEntity> findByInputAssistSetId(
            @Param("inputAssistSetId") BigInteger inputAssistSetId, Pageable pageable);

    /**
     * ブランドコード、デリバリ種別、発注種別から 入力補助セット情報 を検索する.
     *
     * @param brandCode ブランドコード
     * @param deliveryType デリバリ種別
     * @param orderType 発注種別
     * @param pageable Pageable
     * @return 拡張入力補助セット情報
     */
    @Query(value = " SELECT"
            + "    t1.id AS input_assist_set_id"
            + "   ,t2.id AS input_assist_set_detail_id"
            + "   ,t1.set_name"
            + "   ,t2.material_type"
            + "   ,t2.material_id_list"
            + " FROM m_f_input_assist_set t1"
            + " LEFT JOIN m_f_input_assist_set_detail t2"
            + "   ON t1.id = t2.assist_set_id"
            + "   AND t2.deleted_at IS NULL"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.brand_code = :brandCode"
            + " AND t1.delivery_type = :deliveryType"
            + " AND t1.order_type = :orderType"
            + " ORDER BY t1.id ASC, t1.sort_order ASC", nativeQuery = true)
    Page<ExtendedMFInputAssistSetEntity> findByBrandCodeDeliveryTypeOrderType(
            @Param("brandCode") String brandCode,
            @Param("deliveryType") int deliveryType,
            @Param("orderType") int orderType,
            Pageable pageable);
}
