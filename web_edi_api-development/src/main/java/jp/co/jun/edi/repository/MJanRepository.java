package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MJanEntity;

/**
 *
 * MJanNumberRepository.
 *
 */
@Repository
public interface MJanRepository
                extends JpaRepository<MJanEntity, BigInteger>, JpaSpecificationExecutor<MJanEntity>  {

    /**
     * 国コード、事業者コードに合致するJANマスタを取得する.
     *
     * @param countryCode 国コード
     * @param businessCode 事業者コード
     * @param pageable {@link Pageable} instance
     *
     * @return JANマスタエンティティリスト
     */
    @Query("SELECT mjan FROM MJanEntity mjan"
            + " WHERE mjan.countryCode = :countryCode"
            + "   AND mjan.businessCode = :businessCode"
            + "   AND mjan.deletedAt IS NULL")
    Page<MJanEntity> findByCountryCodeAndbusinessCode(
            @Param("countryCode") int countryCode,
            @Param("businessCode") int businessCode,
            Pageable pageable);
}
