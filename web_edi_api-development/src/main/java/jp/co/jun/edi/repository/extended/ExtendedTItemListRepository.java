package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTItemListEntity;

/**
 *
 * ExtendedTItemListRepository.
 *
 */
@Repository
public interface ExtendedTItemListRepository extends JpaRepository<ExtendedTItemListEntity, BigInteger>, JpaSpecificationExecutor<ExtendedTItemListEntity> {

}
