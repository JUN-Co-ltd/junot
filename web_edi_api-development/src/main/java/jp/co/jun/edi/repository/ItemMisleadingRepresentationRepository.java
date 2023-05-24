package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationSearchResultEntity;
import jp.co.jun.edi.repository.custom.ItemMisleadingRepresentationRepositoryCustom;

/**
 * 優良誤認検査承認一覧Repository.
 */
@Repository
public interface ItemMisleadingRepresentationRepository
extends CrudRepository<ExtendedItemMisleadingRepresentationSearchResultEntity, Integer>, ItemMisleadingRepresentationRepositoryCustom {
}
