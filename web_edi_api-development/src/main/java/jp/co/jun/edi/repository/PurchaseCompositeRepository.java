package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.PurchaseCompositeEntity;
import jp.co.jun.edi.repository.custom.PurchaseCompositeRepositoryCustom;

/**
 * 仕入一覧情報Repository.
 */
@Repository
public interface PurchaseCompositeRepository extends CrudRepository<PurchaseCompositeEntity, Integer>, PurchaseCompositeRepositoryCustom {
}
