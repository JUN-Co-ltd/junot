package jp.co.jun.edi.repository.master;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.master.SireEntity;
import jp.co.jun.edi.repository.master.custom.SireRepositoryCustom;

/**
 * 取引先情報Repository.
 */
@Repository
public interface SireRepository extends CrudRepository<SireEntity, Integer>, SireRepositoryCustom {
}
