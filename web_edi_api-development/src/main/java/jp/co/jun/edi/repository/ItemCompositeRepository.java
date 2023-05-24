package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTItemListEntity;
import jp.co.jun.edi.repository.custom.ItemCompositeRepositoryCustom;

/**
 * 品番情報ListRepository.
 */
@Repository
public interface ItemCompositeRepository extends CrudRepository<ExtendedTItemListEntity, Integer>, ItemCompositeRepositoryCustom {
}
