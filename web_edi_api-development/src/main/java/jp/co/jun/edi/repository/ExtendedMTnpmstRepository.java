package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedMTnpmstEntity;
import jp.co.jun.edi.repository.custom.ExtendedMTnpmstRepositoryCustom;

/**
 * ExtendedMTnpmstRepository.
 */
@Repository
public interface ExtendedMTnpmstRepository extends CrudRepository<ExtendedMTnpmstEntity, BigInteger>, ExtendedMTnpmstRepositoryCustom {
}
