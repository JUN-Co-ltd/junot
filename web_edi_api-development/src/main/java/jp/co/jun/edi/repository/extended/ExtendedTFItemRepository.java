package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTFItemEntity;

/**
 *
 * ExtendedTFItemRepository.
 *
 */
@Repository
public interface ExtendedTFItemRepository extends JpaRepository<ExtendedTFItemEntity, BigInteger> {

    /**
     * フクキタル品番IDから フクキタル品番情報+マスタ情報 を検索する.
     * <pre>
     *  SELECT t1.*
     *      ,t2.tape_name AS tape_name
     *      ,t3.tape_width_name AS tape_width_name
     *      ,t29.recycle_name AS recycle_name
     *      ,t30.seal_name AS seal_name
     *      ,t31.product_category_name AS product_category_name
     *      ,t32.product_type_name AS product_type_name
     *    FROM t_f_item t1
     *    LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id
     *    LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id
     *    LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id
     *    LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id
     *    LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id
     *    LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id
     *   WHERE t1.deleted_at IS NULL
     *   AND t1.id = :fItemId -- フクキタル品番ID
     * </pre>
     * @param fItemId フクキタル品番ID
     * @return 拡張フクキタル品番情報を取得する
     */
    @Query(value = "SELECT t1.*"
            + "    ,t2.tape_name AS tape_name"
            + "    ,t3.tape_width_name AS tape_width_name"
            + "    ,t29.recycle_name AS recycle_name"
            + "    ,t30.seal_name AS seal_name"
            + "    ,t31.product_category_name AS product_category_name"
            + "    ,t32.product_type_name AS product_type_name"
            + "  FROM t_f_item t1"
            + "  LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id"
            + "  LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id"
            + "  LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id"
            + "  LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id"
            + "  LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id"
            + "  LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.id = :fItemId", nativeQuery = true)
    Optional<ExtendedTFItemEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId);

    /**
     * 品番IDから フクキタル品番情報+マスタ情報 を検索する.
     * <pre>
     *  SELECT t1.*
     *      ,t2.tape_name AS tape_name
     *      ,t3.tape_width_name AS tape_width_name
     *      ,t29.recycle_name AS recycle_name
     *      ,t30.seal_name AS seal_name
     *      ,t31.product_category_name AS product_category_name
     *      ,t32.product_type_name AS product_type_name
     *    FROM t_f_item t1
     *    LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id
     *    LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id
     *    LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id
     *    LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id
     *    LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id
     *    LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id
     *   WHERE t1.deleted_at IS NULL
     *   AND t1.part_no_id = :partNoId -- 品番ID
     * </pre>
     * @param partNoId 品番ID
     * @return 拡張フクキタル品番情報を取得する
     */
    @Query(value = "SELECT t1.*"
            + "    ,t2.tape_name AS tape_name"
            + "    ,t3.tape_width_name AS tape_width_name"
            + "    ,t29.recycle_name AS recycle_name"
            + "    ,t30.seal_name AS seal_name"
            + "    ,t31.product_category_name AS product_category_name"
            + "    ,t32.product_type_name AS product_type_name"
            + "  FROM t_f_item t1"
            + "  LEFT JOIN m_f_tape t2 ON t2.deleted_at IS NULL AND t1.tape_code = t2.id"
            + "  LEFT JOIN m_f_tape_width t3 ON t3.deleted_at IS NULL AND t1.tape_width_code = t3.id"
            + "  LEFT JOIN m_f_recycle t29 ON t29.deleted_at IS NULL AND t1.recycle_mark = t29.id"
            + "  LEFT JOIN m_f_seal t30 ON t30.deleted_at IS NULL AND t1.sticker_type_code = t30.id"
            + "  LEFT JOIN m_f_cn_product_category t31 ON t31.deleted_at IS NULL AND t1.cn_product_category = t31.id"
            + "  LEFT JOIN m_f_cn_product_type t32 ON t32.deleted_at IS NULL AND t1.cn_product_type = t32.id"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.part_no_id = :partNoId", nativeQuery = true)
    Optional<ExtendedTFItemEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId);

}
