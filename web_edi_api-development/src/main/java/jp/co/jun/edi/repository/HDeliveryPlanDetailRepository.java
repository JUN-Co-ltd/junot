package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.HDeliveryPlanDetailEntity;

/**
 * HDeliveryPlanDetailRepository.
 */
@Repository
public interface HDeliveryPlanDetailRepository extends JpaRepository<HDeliveryPlanDetailEntity, BigInteger> {
}
