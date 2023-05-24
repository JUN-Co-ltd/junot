package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTCompositionEntity;

/**
 *
 * ExtendedTCompositionRepository.
 *
 */
@Repository
public interface ExtendedTCompositionRepository extends JpaRepository<ExtendedTCompositionEntity, BigInteger> {

    /**
     * 品番IDから 組成情報+コード名称 を検索する.
     * メンテ区分＝"3"（削除）分も表示する.
     *
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 拡張組成情報
     */
    @Query(value = "SELECT t.* "
            + "   ,m1.item2 as color_name"
            + "   ,it1.parts_name as parts_name"
            + "   ,m2.item1 as composition_name"
            + " FROM t_composition t"
            + "   LEFT JOIN m_codmst m1 "
            + "          ON m1.tblid = '10' "
            + "         AND m1.mntflg != '3' "
            + "         AND m1.deleted_at is null "
            + "         AND t.color_code = m1.code1 "
            + "   LEFT JOIN m_codmst m2 "
            + "          ON m2.tblid = '13' "
            + "         AND m2.deleted_at is null "
            + "         AND t.composition_code = m2.code1 "
            + "   LEFT JOIN m_item_parts it1 "
            + "          ON it1.id = t.parts_code "
            + "         AND it1.deleted_at is null "
            + " WHERE t.part_no_id = :partNoId "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Page<ExtendedTCompositionEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId, Pageable pageable);

}
