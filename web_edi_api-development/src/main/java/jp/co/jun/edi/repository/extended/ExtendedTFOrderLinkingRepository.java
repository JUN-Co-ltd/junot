package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTFOrderLinkingEntity;

/**
 *
 * ExtendedTFOrderRepository.
 *
 */
@Repository
public interface ExtendedTFOrderLinkingRepository extends JpaRepository<ExtendedTFOrderLinkingEntity, BigInteger> {
    /**
     * 確定済、未連携のフクキタル発注情報 を検索する.
     * @param pageable {@link Pageable}
     * @return 確定済、未連携のフクキタル発注情報を取得する
     */
    @Query(value = " SELECT "
            + "   t1.id "
            + "   , t1.f_item_id "
            + "   , t1.part_no_id "
            + "   , t1.order_id "
            + "   , t1.order_code "
            + "   , (SELECT st3.brand_name FROM m_f_brand st3 WHERE st3.brand_code = t2.brand_code AND st3.order_type = t1.order_type) AS brand_name "
            + "   , t2.brand_code "
            + "   , t1.order_at "
            + "   , (SELECT st3.fukukitaru_account"
            + "      FROM m_f_available_company st3"
            + "      WHERE st3.company = t3.company AND st3.brand_code = t2.brand_code AND st3.deleted_at IS NULL) AS order_user_id " // 発注者コード
            + "   , (SELECT st1.fukukitaru_company_code FROM m_f_destination st1 WHERE st1.id = t1.billing_company_id ) AS billing_company_id "
            + "   , (SELECT st2.fukukitaru_company_code FROM m_f_destination st2 WHERE st2.id = t1.delivery_company_id ) AS delivery_company_id "
            + "   , t1.delivery_staff "
            + "   , CASE WHEN t1.urgent THEN '有' ELSE '無' END AS urgent "
            + "   , t1.preferred_shipping_at "
            + "   , t1.contract_number "
            + "   , t1.special_report "
            + "   , CASE WHEN t1.delivery_type = 1 THEN '国内' ELSE '海外' END AS delivery_type "
            + "   , CASE WHEN t1.repeat_number IS NULL THEN '' ELSE t1.repeat_number END AS repeat_number "
            + "   , t1.mdf_maker_factory_code "
            + "   , t2.part_no"
            + "   , t1.order_type "
            + "   , t1.is_responsible_order"
            + "   , t2.quality_composition_status"
            + "   , t2.quality_coo_status"
            + "   , t2.quality_harmful_status"
            + " FROM "
            + "   t_f_order t1 "
            + " LEFT JOIN t_item t2 ON t2.deleted_at IS NULL AND t2.id = t1.part_no_id "
            + " LEFT JOIN m_user t3 ON t3.deleted_at IS NULL AND t3.id = t1.order_user_id "
            + " WHERE "
            + "       t1.deleted_at IS NULL  "
            + "   AND t1.confirm_status = 1 "
            + "   AND t1.linking_status = 0 "
            + " ORDER BY t1.id ASC ", nativeQuery = true)
    Page<ExtendedTFOrderLinkingEntity> find(Pageable pageable);

}
