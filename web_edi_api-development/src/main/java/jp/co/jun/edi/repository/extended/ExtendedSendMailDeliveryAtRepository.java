package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedSendMailDeliveryAtEntity;

/**
 *
 * ExtendedSendMailDeliveryAtRepository.
 *
 */
@Repository
public interface ExtendedSendMailDeliveryAtRepository extends JpaRepository<ExtendedSendMailDeliveryAtEntity, BigInteger> {

    /**
     * 納品IDをキーに課コードごとに分けて納品日を検索する.
     *
     * @param deliveryId 納品ID
     * @param photoCode 本社撮影の課コード
     * @param sewingCode 縫製検品の課コード
     * @return 納品日
     */
    @Query(value = "SELECT id"
            + "   ,("
            + "     SELECT delivery_at"
            + "     FROM t_delivery_detail"
            + "     WHERE delivery_id = :deliveryId"
            + "         AND division_code = :photoCode"
            + "         AND deleted_at IS NULL"
            + "     LIMIT 1"
            + "   ) AS photo_delivery_at"
            + "   ,("
            + "     SELECT delivery_at"
            + "     FROM t_delivery_detail"
            + "     WHERE delivery_id = :deliveryId"
            + "         AND division_code = :sewingCode"
            + "         AND deleted_at IS NULL"
            + "     LIMIT 1"
            + "   ) AS sewing_delivery_at"
            + "   ,("
            + "     SELECT delivery_at"
            + "     FROM t_delivery_detail"
            + "     WHERE delivery_id = :deliveryId"
            + "         AND division_code NOT IN (:photoCode, :sewingCode)"
            + "         AND deleted_at IS NULL"
            + "     LIMIT 1"
            + "   ) AS delivery_at"
            + "   FROM t_delivery_detail"
            + "   WHERE delivery_id = :deliveryId"
            + "     AND deleted_at IS NULL"
            + "   LIMIT 1", nativeQuery = true)
    Optional<ExtendedSendMailDeliveryAtEntity> findDeliveryAt(
            @Param("deliveryId") BigInteger deliveryId,
            @Param("photoCode") String photoCode,
            @Param("sewingCode") String sewingCode
            );
}
