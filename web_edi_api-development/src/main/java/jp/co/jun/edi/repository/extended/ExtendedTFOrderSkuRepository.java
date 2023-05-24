package jp.co.jun.edi.repository.extended;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTFOrderSkuEntity;

/**
 *
 * ExtendedTFOrderSkuRepository.
 *
 */
@Repository
public interface ExtendedTFOrderSkuRepository extends JpaRepository<ExtendedTFOrderSkuEntity, BigInteger> {

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_wash_name m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：洗濯ネーム @link{FukukitaruMasterMaterialType.WASH_NAME}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_wash_name m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialWashName(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_attention_hang_tag m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：アテンション下札 @link{FukukitaruMasterMaterialType.ATTENTION_NAME}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_attention_hang_tag m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialAttentionHangTag(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報アテンションネーム を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_attention_name m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：アテンションネーム @link{FukukitaruMasterMaterialType.ATTENTION_NAME}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_attention_name m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialAttentionName(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報アテンションタグ を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_attention_tag m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：アテンションタグ @link{FukukitaruMasterMaterialType.ATTENTION_TAG}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_attention_tag m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialAttentionTag(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報下札 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_hang_tag m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：下札 @link{FukukitaruMasterMaterialType.HANG_TAG}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_hang_tag m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialHangTag(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報下札同封副資材 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_hang_tag_auxiliary m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：下札同封副資材 @link{FukukitaruMasterMaterialType.HANG_TAG_AUXILIARY_MATERIAL}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_hang_tag_auxiliary m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialHangTagAuxiliary(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報洗濯同封副資材 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_wash_auxiliary m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：洗濯同封副資材 @link{FukukitaruMasterMaterialType.WASH_AUXILIARY_MATERIAL}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_wash_auxiliary m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialWashAuxiliary(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * フクキタル発注IDから フクキタル発注SKU情報+フクキタル用資材情報 を検索する.
     * <pre>
     *  SELECT t.*
     *    ,m.material_type_name
     *    ,m.material_type
     *    ,m.material_code
     *    ,m.material_code_name
     *  FROM t_f_order_sku t
     *  INNER JOIN m_f_material_hang_tag_nergy_merit m
     *    ON m.deleted_at IS NULL
     *    AND m.id = t.material_id
     *  WHERE t.f_order_id = 1
     *  AND t.material_type = 1
     *  AND t.deleted_at IS NULL
     *  ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC
     * </pre>
     * @param fOrderId フクキタル発注ID
     * @param materialType 資材種別：洗濯ネーム @link{FukukitaruMasterMaterialType.WASH_NAME}
     * @param pageable {@link Pageable}
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT t.* "
            + "   ,m.material_type_name"
            + "   ,m.material_type"
            + "   ,m.material_code"
            + "   ,m.material_code_name"
            + " FROM t_f_order_sku t"
            + " INNER JOIN m_f_material_hang_tag_nergy_merit m "
            + "   ON m.deleted_at IS NULL "
            + "   AND m.id = t.material_id "
            + " WHERE t.f_order_id = :fOrderId "
            + " AND t.material_type = :materialType"
            + " AND t.deleted_at IS NULL "
            + " ORDER BY t.material_type ASC, t.color_code ASC, t.sort_order ASC", nativeQuery = true)
    Page<ExtendedTFOrderSkuEntity> findByFOrderIdJoinMaterialHangTagNergyMerit(
            @Param("fOrderId") BigInteger fOrderId,
            @Param("materialType") int materialType,
            Pageable pageable);

    /**
     * 洗濯ネーム発注数合計を取得.
     *
     * @param fOrderId フクキタル発注ID
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT sum(t1.order_lot) AS totalLot"
            + " FROM t_f_order_sku t1"
            + " WHERE t1.deleted_at IS NULL"
            + "   AND t1.f_order_id = :fOrderId"
            + "   AND t1.material_type = 1", nativeQuery = true)
    BigDecimal totalCountWashName(
            @Param("fOrderId") BigInteger fOrderId);

    /**
     * 下札発注数合計を取得.
     *
     * @param fOrderId フクキタル発注ID
     * @return 拡張フクキタル発注SKU情報を取得する
     */
    @Query(value = " SELECT sum(t1.order_lot) AS totalLot"
            + " FROM t_f_order_sku t1"
            + " WHERE t1.deleted_at IS NULL"
            + "   AND t1.f_order_id = :fOrderId"
            + "   AND t1.material_type = 4"
            + " GROUP BY t1.material_id"
            + " ORDER BY totalLot DESC"
            + " LIMIT 1", nativeQuery = true)
    BigDecimal totalCountHangTag(
            @Param("fOrderId") BigInteger fOrderId);
}
