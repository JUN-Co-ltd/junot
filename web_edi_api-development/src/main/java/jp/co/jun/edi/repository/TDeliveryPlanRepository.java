package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryPlanEntity;

/**
 * TDeliveryPlanRepository.
 */
@Repository
public interface TDeliveryPlanRepository extends JpaRepository<TDeliveryPlanEntity, BigInteger>, JpaSpecificationExecutor<TDeliveryPlanEntity> {

}
