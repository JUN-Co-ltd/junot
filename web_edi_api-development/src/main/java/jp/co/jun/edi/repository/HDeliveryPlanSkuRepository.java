package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.HDeliveryPlanSkuEntity;

/**
 * HDeliveryPlanSkuRepository.
 */
@Repository
public interface HDeliveryPlanSkuRepository extends JpaRepository<HDeliveryPlanSkuEntity, BigInteger> {

}
