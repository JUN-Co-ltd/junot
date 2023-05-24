package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedDistributionShipmentSearchResultEntity;
import jp.co.jun.edi.repository.custom.DistributionShipmentSearchResultCompositeRepositoryCustom;

/**
 * ExtendedDistributionShipmentDeliveryStoreRepository.
 */
@Repository
public interface DistributionShipmentSearchResultCompositeRepository
extends CrudRepository<ExtendedDistributionShipmentSearchResultEntity, BigInteger>,
DistributionShipmentSearchResultCompositeRepositoryCustom {
}
