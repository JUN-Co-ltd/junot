//PRD_0137 #10669 add start
package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.DynamicMaintCodeEntity;
import jp.co.jun.edi.repository.custom.DynamicMaintSizeRepositoryCustom;

/**
 * DynamicMaintCodeRepository.
 */
@Repository
public interface DynamicMaintSizeRepository extends CrudRepository<DynamicMaintCodeEntity, Integer>, DynamicMaintSizeRepositoryCustom {
}
//PRD_0137 #10669 add end