package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTFItemLinkingEntity;

/**
 *
 * ExtendedTFItemRepository.
 *
 */
@Repository
public interface ExtendedTFItemLinkingRepository extends JpaRepository<ExtendedTFItemLinkingEntity, BigInteger> {

    /**
     * フクキタル品番IDから フクキタル品番情報 を検索する.
     * <pre>
     *   SELECT
     *           t1.id
     *          ,t1.reefur_private_brand_code
     *          ,CONCAT(SUBSTRING(t33.part_no,1,3),'-',SUBSTRING(t33.part_no,4)) AS part_no
     *          ,t33.season_code
     *          ,t4.category_code
     *          ,t1.saturdays_private_ny_part_no
     *          ,CASE WHEN t1.print_coo = 0 THEN '' ELSE t35.coo_name END AS coo_name
     *          ,t33.retail_price
     *          ,'' AS item_name
     *          ,'' AS item_type
     *          ,t2.tape_code AS tape_name
     *          ,t3.tape_width_code AS tape_width_name
     *          ,CASE WHEN t1.print_size=1 THEN  '有' ELSE '無' END AS print_size
     *          ,t1.nergy_bill_code1
     *          ,t1.nergy_bill_code2
     *          ,t1.nergy_bill_code3
     *          ,t1.nergy_bill_code4
     *          ,t1.nergy_bill_code5
     *          ,t1.nergy_bill_code6
     *          ,CASE WHEN t1.print_qrcode THEN '有' ELSE '無' END AS print_qrcode
     *          ,CASE WHEN t1.print_wash_pattern THEN '有' ELSE '無' END AS print_wash_pattern
     *          ,CASE WHEN t1.print_appendices_term THEN  '有' ELSE '無' END AS print_appendices_term
     *          ,CASE WHEN t1.print_parts = 1 THEN '有' ELSE '無' END AS print_parts
     *          ,t29.recycle_code AS recycle_code
     *          ,t30.seal_code AS seal_code
     *          ,t31.product_category_code AS cn_product_category_code
     *          ,t32.product_type_code AS cn_product_type_code
     *          ,'' AS cn_coo_code
     *          ,CASE WHEN  t1.print_sustainable_mark THEN '有' ELSE '無' END AS print_sustainable_mark
     *      FROM t_f_item t1
     *      LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id
     *      LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id
     *      LEFT JOIN m_f_category_code t4 ON t4.deleted_at IS NULL AND t1.category_code = t4.id
     *      LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id
     *      LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id
     *      LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id
     *      LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id
     *      LEFT JOIN t_item t33 ON t33.deleted_at IS NULL AND t1.part_no_id = t33.id
     *      LEFT JOIN t_order t36 ON ON t36.deleted_at IS NULL AND t36.id = :orderId"
     *      LEFT JOIN m_f_country t35 ON t35.deleted_at IS NULL AND t36.coo_code = t35.coo_code
     *      WHERE t1.deleted_at IS NULL
     *      AND t1.id = 1 -- :fItemId ★フクキタル品番ID
     * </pre>
     * @param fItemId フクキタル品番ID
     * @param orderId 発注ID
     * @return 拡張フクキタル品番情報を取得する
     */
    @Query(value = " SELECT "
                    + "      t1.id "
                    + "     ,t1.reefur_private_brand_code "
                    + "     ,CONCAT(SUBSTRING(t33.part_no,1,3),'-',SUBSTRING(t33.part_no,4)) AS part_no "
                    + "     ,t33.season_code "
                    + "     ,t4.category_code "
                    + "     ,t1.saturdays_private_ny_part_no "
                    + "     ,CASE WHEN t1.print_coo = 0 THEN '' ELSE t35.coo_name END AS coo_name "
                    + "     ,t33.retail_price "
                    + "     ,'' AS item_name "
                    + "     ,'' AS item_type "
                    + "     ,t2.tape_code AS tape_name "
                    + "     ,t3.tape_width_code AS tape_width_name "
                    + "     ,CASE WHEN t1.print_size=1 THEN  '有' ELSE '無' END AS print_size "
                    + "     ,t1.nergy_bill_code1 "
                    + "     ,t1.nergy_bill_code2 "
                    + "     ,t1.nergy_bill_code3 "
                    + "     ,t1.nergy_bill_code4 "
                    + "     ,t1.nergy_bill_code5 "
                    + "     ,t1.nergy_bill_code6 "
                    + "     ,CASE WHEN t1.print_qrcode THEN '有' ELSE '無' END AS print_qrcode "
                    + "     ,CASE WHEN t1.print_wash_pattern THEN '有' ELSE '無' END AS print_wash_pattern "
                    + "     ,CASE WHEN t1.print_appendices_term THEN  '有' ELSE '無' END AS print_appendices_term "
                    + "     ,CASE WHEN t1.print_parts = 1 THEN '有' ELSE '無' END AS print_parts "
                    + "     ,t29.recycle_code AS recycle_code "
                    + "     ,t30.seal_code AS seal_code "
                    + "     ,t31.product_category_code AS cn_product_category_code "
                    + "     ,t32.product_type_code AS cn_product_type_code "
                    + "     ,'' AS cn_coo_code "
                    + "     ,CASE WHEN  t1.print_sustainable_mark THEN '有' ELSE '無' END AS print_sustainable_mark"
                    + " FROM t_f_item t1 "
                    + " LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id "
                    + " LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id "
                    + " LEFT JOIN m_f_category_code t4 ON t4.deleted_at IS NULL AND t1.category_code = t4.id "
                    + " LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id "
                    + " LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id "
                    + " LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id "
                    + " LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id "
                    + " LEFT JOIN t_item t33 ON t33.deleted_at IS NULL AND t1.part_no_id = t33.id "
                    + " LEFT JOIN t_order t36 ON t36.deleted_at IS NULL AND t36.id = :orderId"
                    + " LEFT JOIN m_f_country t35 ON t35.deleted_at IS NULL AND t36.coo_code = t35.coo_code "
                    + " WHERE t1.deleted_at IS NULL "
                    + " AND t1.id = :fItemId ", nativeQuery = true)
    Optional<ExtendedTFItemLinkingEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId,
            @Param("orderId") BigInteger orderId);

}
