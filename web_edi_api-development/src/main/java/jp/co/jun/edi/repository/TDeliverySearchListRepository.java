package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliverySearchResultEntity;
import jp.co.jun.edi.repository.custom.TDeliverySearchListRepositoryCustom;

/**
 * 配分一覧Repository.
 */
@Repository
public interface TDeliverySearchListRepository extends CrudRepository<TDeliverySearchResultEntity, Integer>, TDeliverySearchListRepositoryCustom {
}
