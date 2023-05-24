package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTOrderSkuEntity;

/**
 *
 * ExtendedTOrderSkuRepository.
 * orderNumberは受注確定されるまで、"000000"が設定されるため.
 * orderNumberを条件にしてSQLを実行すると想定外のレコードも.
 * 編集される可能性があるので、orderNumberは条件に極力使用しないこと.
 *
 */
@Repository
public interface ExtendedTOrderSkuRepository extends JpaRepository<ExtendedTOrderSkuEntity, BigInteger> {

    /**
     * 発注IDから発注SKU情報+コード名称 を検索する.
     *
     * @param orderId 発注ID
     * @param partNoKind 品種
     * @param pageable pageable
     * @return 拡張発注SKU情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,m1.item2 as color_name"
            + "   ,CAST(m2.jun as SIGNED) sort_order"
            + " FROM t_order_sku t"
            + "   LEFT JOIN m_codmst m1 "
            + "          ON m1.tblid = '10' "
            + "         AND m1.mntflg != '3' "
            + "         AND m1.deleted_at is null "
            + "         AND t.color_code = m1.code1 "
            + "   LEFT JOIN m_sizmst m2 "
            + "          ON m2.hscd = :partNoKind "
            + "         AND m2.szkg =  t.size"
            + "         AND m2.mntflg IN ('1', '2', '')"
            + " WHERE t.order_id = :orderId "
            + " AND t.deleted_at is null "
            + " ORDER BY color_code ASC, sort_order ASC", nativeQuery = true)
    Page<ExtendedTOrderSkuEntity> findByOrderId(
            @Param("orderId") BigInteger orderId,
            @Param("partNoKind") String partNoKind,
            Pageable pageable);

}
