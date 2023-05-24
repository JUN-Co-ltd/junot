package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TDeliveryFileInfoEntity;

/**
 *
 * TDeliveryFileInfoRepository.
 *
 */
@Repository
public interface TDeliveryFileInfoRepository extends JpaRepository<TDeliveryFileInfoEntity, BigInteger>, JpaSpecificationExecutor<TDeliveryFileInfoEntity> {
    /**
     * 指定した発注IDに紐づく納品依頼ファイル情報リストを取得する.
     * ただしfile_no_idがt_fileのidと紐付かないレコードは取得しない.
     *
     * @param orderId 発注ID
     * @param pageable pageable
     * @return 納品依頼ファイル情報
     */
    @Query("SELECT df FROM TDeliveryFileInfoEntity df"
            + " WHERE df.orderId = :orderId "
            + " AND df.deletedAt IS NULL"
            + " AND EXISTS ("
            + "   SELECT f FROM TFileEntity f"
            + "   WHERE df.fileNoId = f.id"
            + "   AND f.deletedAt IS NULL)")
    List<TDeliveryFileInfoEntity> findByOrderIdAndExistsTfile(
            @Param("orderId") BigInteger orderId, Pageable pageable);
}
