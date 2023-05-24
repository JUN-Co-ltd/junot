package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MDeliveryCountNumberEntity;

/**
 * 納品依頼回数採番マスタのRepository.
 */
public interface MDeliveryCountNumberRepository extends JpaRepository<MDeliveryCountNumberEntity, BigInteger> {

    /**
     * 発注IDをキーに採番マスタデータを取得.
     *
     * @param orderId 発注ID
     * @return 納品依頼回数採番マスタエンティティ
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MDeliveryCountNumberEntity m"
            + " WHERE m.orderId = :orderId"
            + " AND m.deletedAt IS NULL")
    Optional<MDeliveryCountNumberEntity> findByOrderId(
            @Param("orderId") BigInteger orderId);

}
