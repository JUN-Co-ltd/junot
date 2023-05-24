package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTOrderLinkingEntity;

/**
 *
 * ExtendedTOrderLinkingRepository.
 *
 */
@Repository
public interface ExtendedTOrderLinkingRepository extends JpaRepository<ExtendedTOrderLinkingEntity, BigInteger> {

    /**
     * 発注IDから 生産メーカー を検索する.
     *
     * @param orderId
     *            発注ID
     * @return 生産メーカーを取得する
     */
    @Query(value = " SELECT "
            + "    t1.id "
            + "  , CONCAT(t2.sire ,t2.name)  AS sire "
            + " FROM t_order t1"
            + " LEFT JOIN m_sirmst t2 "
            + "   ON t2.deleted_at IS NULL "
            + "   AND t2.sire = t1.mdf_maker_code "
            + "   AND t2.mntflg IN ('1', '2', '') "
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.id = :orderId", nativeQuery = true)
    Optional<ExtendedTOrderLinkingEntity> findByOrderId(@Param("orderId") BigInteger orderId);

}
