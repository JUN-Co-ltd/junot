package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TOrderFileInfoEntity;

/**
 *
 * TOrderFileInfoRepository.
 *
 */
@Repository
public interface TOrderFileInfoRepository extends JpaRepository<TOrderFileInfoEntity, BigInteger> {

    /**
     * 発注IDから 発注ファイル情報 を検索する.
     * ただしfile_no_idがt_fileのidと紐付かないレコードは取得しない.
     * order_idが重複する場合、id 降順の最初のレコードを取得する.
     * @param orderId 発注ID
     * @param pageable ページ情報
     * @return 発注ファイル情報
     */
    @Query("SELECT t FROM TOrderFileInfoEntity t"
            + " WHERE t.orderId = :orderId "
            + " AND t.deletedAt is null"
            + " AND EXISTS ("
            + "   SELECT f FROM TFileEntity f"
            + "   WHERE t.fileNoId = f.id"
            + "   AND f.deletedAt IS NULL)")
    Page<TOrderFileInfoEntity> findByOrderIdAndExistsTfile(
           @Param("orderId") BigInteger orderId, Pageable pageable);

    /**
     * 発注ID配列から発注ファイル情報を取得する.
     * ただしfile_no_idがt_fileのidと紐付かないレコードは取得しない.
     * order_idが重複する場合、id 降順の最初のレコードを取得する.
     * @param orderIds 発注ID配列
     * @return 発注ファイル情報リスト
     */
    @Query(
            "SELECT t1 FROM TOrderFileInfoEntity t1"
            + " WHERE t1.id IN ("
            + "   SELECT max(t2.id) FROM TOrderFileInfoEntity t2"
            + "   WHERE t2.orderId IN (:orderIds)"
            + "   AND t2.deletedAt is null"
            + "   GROUP BY t2.orderId"
            + " )"
            + " AND EXISTS ("
            + "   SELECT f FROM TFileEntity f"
            + "   WHERE t1.fileNoId = f.id"
            + "   AND f.deletedAt IS NULL)")
    List<TOrderFileInfoEntity> findByOrderIdsAndExistsTfile(
            @Param("orderIds") List<BigInteger> orderIds);

    /**
     * ※発注削除で使用
     * 発注IDから 発注ファイル情報 を検索する.
     * @param orderId 発注ID
     * @param pageable ページ情報
     * @return 発注ファイル情報
     */
    @Query("SELECT t FROM TOrderFileInfoEntity t" + " WHERE t.orderId = :orderId AND t.deletedAt is null")
    Page<TOrderFileInfoEntity> findByOrderId(@Param("orderId") BigInteger orderId, Pageable pageable);
}
