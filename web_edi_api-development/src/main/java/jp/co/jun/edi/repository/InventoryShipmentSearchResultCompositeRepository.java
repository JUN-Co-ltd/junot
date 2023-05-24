package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTInventoryShipmentSearchResultEntity;
import jp.co.jun.edi.repository.custom.InventoryShipmentSearchResultCompositeRepositoryCustom;

/**
 * InventoryShipmentSearchResultCompositeRepository.
 */
@Repository
public interface InventoryShipmentSearchResultCompositeRepository
extends CrudRepository<ExtendedTInventoryShipmentSearchResultEntity, BigInteger>,
InventoryShipmentSearchResultCompositeRepositoryCustom {
}
