package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MUnusableJanEntity;

/**
 *
 * MUnusableJanRepository.
 *
 */
@Repository
public interface MUnusableJanRepository
                extends JpaRepository<MUnusableJanEntity, BigInteger>, JpaSpecificationExecutor<MUnusableJanEntity>  {

    /**
     * JANコードに合致する使用不可JANマスタを取得する.
     *
     * @param janCode JANコード
     *
     * @return 使用不可JANEnityt
     */
    @Query("SELECT mujan FROM MUnusableJanEntity mujan"
            + " WHERE mujan.janCode = :janCode"
            + "   AND mujan.deletedAt IS NULL")
    Optional<MUnusableJanEntity> findByJanCode(
            @Param("janCode") String janCode);
}
