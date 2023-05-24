package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MakerReturnProductCompositeEntity;
import jp.co.jun.edi.repository.custom.MakerReturnProductCompositeRepositoryCustom;

/**
 * メーカー返品商品情報Repository.
 */
@Repository
public interface MakerReturnProductCompositeRepository
extends CrudRepository<MakerReturnProductCompositeEntity, Integer>, MakerReturnProductCompositeRepositoryCustom {
}
