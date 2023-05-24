package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryEntity;
import jp.co.jun.edi.repository.custom.DeliveryLotRepositoryCustom;

/**
 * DeliveryLotRepository.
 */
@Repository
public interface DeliveryLotRepository extends CrudRepository<TDeliveryEntity, Integer>, DeliveryLotRepositoryCustom {
}
