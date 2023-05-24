package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.repository.custom.MakerReturnCompositeRepositoryCustom;

/**
 * メーカー返品一覧Repository.
 */
@Repository
public interface MakerReturnCompositeRepository extends CrudRepository<TMakerReturnEntity, Integer>, MakerReturnCompositeRepositoryCustom {
}
