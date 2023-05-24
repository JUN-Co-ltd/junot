package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.VDelischeDeliveryRequestEntity;
import jp.co.jun.edi.entity.key.VDelischeDeliveryRequestKey;
import jp.co.jun.edi.repository.custom.VDelischeDeliveryRequestRepositoryCustom;

/**
 * デリスケ納品依頼Repository.
 */
@Repository
public interface VDelischeDeliveryRequestRepository extends CrudRepository<VDelischeDeliveryRequestEntity, VDelischeDeliveryRequestKey>,
VDelischeDeliveryRequestRepositoryCustom {
}
