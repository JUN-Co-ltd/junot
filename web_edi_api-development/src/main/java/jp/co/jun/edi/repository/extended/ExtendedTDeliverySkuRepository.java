package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTDeliverySkuEntity;

/**
 *
 * ExtendedTDeliverySkuRepository.
 *
 */
@Repository
public interface ExtendedTDeliverySkuRepository extends JpaRepository<ExtendedTDeliverySkuEntity, BigInteger> {
}
