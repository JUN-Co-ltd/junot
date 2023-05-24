package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.VDelischeOrderEntity;
import jp.co.jun.edi.repository.custom.VDelischeOrderRepositoryCustom;

/**
 * デリスケ発注Repository.
 */
@Repository
public interface VDelischeOrderRepository extends CrudRepository<VDelischeOrderEntity, Integer>, VDelischeOrderRepositoryCustom {
}
