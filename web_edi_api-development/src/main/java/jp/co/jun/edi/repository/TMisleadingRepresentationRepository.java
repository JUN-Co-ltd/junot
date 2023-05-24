package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TMisleadingRepresentationEntity;

/**
 *
 * TMisleadingRepresentationRepository.
 *
 */
@Repository
public interface TMisleadingRepresentationRepository extends JpaRepository<TMisleadingRepresentationEntity, BigInteger> {

    /**
     * 品番に紐づく優良誤認情報を取得する.
     *
     * @param partNoId 品番ID
     * @param pageable pageable
     * @return 優良誤認情報
     */
    @Query("SELECT t FROM TMisleadingRepresentationEntity t"
            + " WHERE t.partNoId = :partNoId"
            + " AND t.deletedAt is null")
    Page<TMisleadingRepresentationEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId,
            Pageable pageable);
}
