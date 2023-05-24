package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTFAttentionAppendicesTermEntity;

/**
 *
 * ExtendedTFWashPatternRepository.
 *
 */
@Repository
public interface ExtendedTFAttentionAppendicesTermRepository extends JpaRepository<ExtendedTFAttentionAppendicesTermEntity, BigInteger> {

    /**
     * フクキタル品番IDから フクキタル絵表示情報+マスタ情報 を検索する.
     * <pre>
     *   SELECT t1.*
     *     ,t2.appendices_term_code
     *     ,t2.appendices_term_code_name
     *   FROM t_f_attention_appendices_term t1
     *   LEFT JOIN m_f_appendices_term t2 ON t1.appendices_term_id = t2.id AND t2.deleted_at IS NULL
     *   WHERE t1.deleted_at IS NULL
     *   AND t1.f_item_id = 4
     *   ORDER BY t1.color_code ASC, t2.appendices_term_code ASC;
     * </pre>
     * @param fItemId フクキタル品番ID
     * @param pageable Pageable
     * @return 拡張フクキタル品番情報を取得する
     */
    @Query(value = " SELECT"
            + "    t1.*"
            + "   ,t2.appendices_term_code"
            + "   ,t2.appendices_term_code_name"
            + " FROM t_f_attention_appendices_term t1"
            + " LEFT JOIN m_f_appendices_term t2 ON t1.appendices_term_id = t2.id AND t2.deleted_at IS NULL"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.f_item_id = :fItemId"
            + " ORDER BY t1.color_code ASC, t2.appendices_term_code ASC", nativeQuery = true)
    Page<ExtendedTFAttentionAppendicesTermEntity> findByFItemId(
            @Param("fItemId") BigInteger fItemId, Pageable pageable);

}
