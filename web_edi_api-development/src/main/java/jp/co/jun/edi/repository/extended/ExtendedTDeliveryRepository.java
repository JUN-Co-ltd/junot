package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTDeliveryEntity;

/**
 *
 * ExtendedTDeliveryRepository.
 *
 */
@Repository
public interface ExtendedTDeliveryRepository extends JpaRepository<ExtendedTDeliveryEntity, BigInteger> {

    /**
     * 納品Idから 納品情報+コード名称 を検索する.
     *
     * @param id
     *            納品Id
     * @return 拡張納品情報を取得する
     */
    @Query(value = "SELECT t.*"
            + "   ,u.account_name as sq_lock_user_account_name"
            + " FROM t_delivery t"
            + "   LEFT JOIN m_user u "
            + "         ON u.deleted_at is null "
            + "         AND t.sq_lock_user_id = u.id "
            + " WHERE t.id = :id "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Optional<ExtendedTDeliveryEntity> findById(
            @Param("id") BigInteger id);

}
