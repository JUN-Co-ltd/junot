package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TPurchaseFileInfoEntity;
//PRD_0134 #10654 add JEF start
/**
 * TMakerReturnFileInfoRepository.
 */
@Repository
public interface TPurchaseFileInfoRepository extends JpaRepository<TPurchaseFileInfoEntity, BigInteger> {
    /**
     * 仕入ファイル情報のステータスを更新する.
     * @param status ステータス
     * @param purchase_voucher_number 伝票No
     * @return 仕入ファイル情報
     */
    @Modifying
    @Query(value = "UPDATE t_purchase_file_info t"
            + " SET t.status = 2, "
            + " t.updated_at = now(), "
            + " t.updated_user_id = :userId"
            + " WHERE t.purchase_voucher_number = :voucherNumber", nativeQuery = true)
    int updateStatusByVoucherNumber(
            @Param("voucherNumber") String voucherNumber,
            @Param("userId") BigInteger userId);

    /**
     * 仕入ファイル情報から、未送信かつ消化委託のデータを取得する
     * @param status 状態
     * @param yyyyMMfrom
     * @param yyyyMMto
     * @param pageable Pageable
     * @return 仕入ファイル情報
     */
    @Query(value = "SELECT tpif.* FROM t_purchase_file_info tpif"
    		+ " INNER JOIN t_purchase tp"
    		+ " ON tpif.purchase_voucher_number = tp.purchase_voucher_number"
    		+ " AND tpif.created_at BETWEEN :yyyyMMfrom AND :yyyyMMto "
            + " WHERE tpif.status = 0"
    		+ " AND ((tp.purchase_type = 9 AND tp.arrival_place = 19)OR(tp.purchase_type = 3 AND tp.arrival_place = 19))"
            + "   AND tpif.deleted_at IS NULL" , nativeQuery = true)
    Page<TPurchaseFileInfoEntity> findBySendStatus(
             @Param("yyyyMMfrom") String yyyyMMfrom,@Param("yyyyMMto") String yyyyMMto, Pageable pageable);
}
//PRD_0134 #10654 add JEF end