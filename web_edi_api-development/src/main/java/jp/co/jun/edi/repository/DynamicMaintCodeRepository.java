package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.DynamicMaintCodeEntity;
import jp.co.jun.edi.repository.custom.DynamicMaintCodeRepositoryCustom;

/**
 * DynamicMaintCodeRepository.
 */
@Repository
public interface DynamicMaintCodeRepository extends CrudRepository<DynamicMaintCodeEntity, Integer>, DynamicMaintCodeRepositoryCustom {
}
