package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.jun.edi.entity.MThresholdEntity;

/**
 * 閾値マスタのRepository.
 */
public interface MThresholdRepository extends JpaRepository<MThresholdEntity, BigInteger>, JpaSpecificationExecutor<MThresholdEntity>  {

}
