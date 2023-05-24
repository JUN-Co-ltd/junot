package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MJanNumberEntity;

/**
 *
 * MJanNumberRepository.
 *
 */
@Repository
public interface MJanNumberRepository
        extends JpaRepository<MJanNumberEntity, BigInteger>, JpaSpecificationExecutor<MJanNumberEntity> {
    /**
     * ブランドに紐づく会社が保持しているJANのうち、最小のIDを取得する.
     * 採番可能なレコードを全てロックする。
     *
     * @param janIds JANマスタIDのリスト
     * @return 採番マスタエンティティ
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT MIN(mjn.id) FROM MJanNumberEntity mjn"
            + " WHERE mjn.janId IN (:janIds)"
            + "   AND mjn.deletedAt IS NULL"
            + "   AND mjn.usedFlg = 0")
    Optional<BigInteger> getMinId(
            @Param("janIds") List<BigInteger> janIds);
}
