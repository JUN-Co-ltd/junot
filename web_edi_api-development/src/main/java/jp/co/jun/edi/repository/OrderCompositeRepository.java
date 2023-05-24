package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.OrderCompositeEntity;
import jp.co.jun.edi.repository.custom.OrderCompositeRepositoryCustom;

/**
 * 発注一覧検索Repository.
 */
@Repository
public interface OrderCompositeRepository extends CrudRepository<OrderCompositeEntity, Integer>, OrderCompositeRepositoryCustom {

}
