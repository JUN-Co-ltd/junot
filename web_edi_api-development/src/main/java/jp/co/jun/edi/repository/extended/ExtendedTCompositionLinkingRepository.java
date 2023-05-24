package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTCompositionLinkingEntity;

/**
 *
 * ExtendedTCompositionRepository.
 *
 */
@Repository
public interface ExtendedTCompositionLinkingRepository extends JpaRepository<ExtendedTCompositionLinkingEntity, BigInteger> {

    /**
     * 品番IDから 組成情報 を検索する.
     * メンテ区分＝"3"（削除）分も表示する.
     *
     * <pre>
     * パーツが、その他フラグ(1)の場合、空文字を設定する.
     * 混率が、ゼロの場合、NULLを設定する.
     *  SELECT
     *     t1.id
     *    ,t1.color_code
     *    ,(SELECT
     *        CASE WHEN st2.other_parts_flg = 1 THEN '' ELSE st2.parts_name END parts_name
     *      FROM m_item_parts st2
     *      WHERE st2.deleted_at IS NULL
     *      AND st2.id = t1.parts_code) AS parts
     *    ,t1.composition_code
     *    ,(SELECT
     *        st1.item1
     *      FROM m_codmst st1
     *      WHERE st1.deleted_at IS NULL
     *            AND st1.tblid = '13'
     *            AND st1.code1 = t1.composition_code) AS composition
     *    ,CASE WHEN t1.percent = 0 THEN NULL ELSE t1.percent END percent
     *  FROM t_composition t1
     *  WHERE
     *        t1.deleted_at IS NULL
     *    AND t1.part_no_id = :partNoId
     *  ORDER BY color_code ASC, serial_number ASC
     *  </pre>
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 拡張組成情報
     */
    @Query(value = " SELECT "
            + "    t1.id"
            + "   ,t1.color_code "
            + "   ,(SELECT "
            + "       CASE WHEN st2.other_parts_flg = 1 THEN '' ELSE st2.parts_name END parts_name "
            + "     FROM m_item_parts st2 "
            + "     WHERE st2.deleted_at IS NULL "
            + "           AND st2.id = t1.parts_code) AS parts "
            + "   ,t1.composition_code "
            + "   ,(SELECT "
            + "       st1.item1"
            + "     FROM m_codmst st1 "
            + "     WHERE st1.deleted_at IS NULL "
            + "           AND st1.tblid = '13' "
            + "           AND st1.code1 = t1.composition_code) AS composition "
            + "   ,CASE WHEN t1.percent = 0 THEN NULL ELSE t1.percent END percent "
            + " FROM t_composition t1 "
            + " WHERE "
            + "       t1.deleted_at IS NULL "
            + "   AND t1.part_no_id = :partNoId "
            + " ORDER BY color_code ASC, serial_number ASC", nativeQuery = true)
    Page<ExtendedTCompositionLinkingEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId, Pageable pageable);

}
