package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.HDeliveryPlanCutEntity;

/**
 * HDeliveryPlanCutRepository.
 */
@Repository
public interface HDeliveryPlanCutRepository extends JpaRepository<HDeliveryPlanCutEntity, BigInteger> {
}
