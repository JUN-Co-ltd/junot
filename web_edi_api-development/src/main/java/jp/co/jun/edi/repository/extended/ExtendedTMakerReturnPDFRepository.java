package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnPDFEntity;

/**
 * ExtendedTMakerReturnPDFRepository.
 */
@Repository
public interface ExtendedTMakerReturnPDFRepository extends JpaRepository<ExtendedTMakerReturnPDFEntity, BigInteger> {
    /**
     * 返品明細PDF作成用SQL.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @param pageable pageable
     * @return 返品明細PDF情報を取得する
     */
    @Query(value = " SELECT"
            + "  t1.id as id" // ID
            + ", t1.voucher_number as voucher_number" // 伝票番号
            + ", t1.voucher_line as voucher_line" // 伝票番号行
            + ", t1.size as size" // サイズ
            + ", t1.color_code as color_code" // カラーコード
            + ", (SELECT sq1.item2 FROM m_codmst sq1" // カラーコード名
            + "     WHERE sq1.deleted_at IS NULL "
            + "     AND sq1.mntflg IN ('1', '2', '')"
            + "     AND sq1.tblid='10' "
            + "     AND sq1.code1 = t1.color_code ) as color_code_name"
            + ", CAST((SELECT sq2.jun FROM m_sizmst sq2" // カラーコード名
            + "     WHERE sq2.deleted_at IS NULL "
            + "     AND sq2.mntflg IN ('1', '2', '')"
            + "     AND sq2.hscd = CONCAT(t2.brand_code, t2.item_code) "
            + "     AND sq2.szkg = t1.size ) as SIGNED) sort_order"
            + ", '0' as plans_number" // 予定数(固定)"
            // PRD_0132 #10060 mod JFE start
            //+ ", t1.return_lot as confirm_number" // 確定数
            + ", t1.fix_return_lot as confirm_number" // 確定数
            // PRD_0132 #10060 mod JFE end
            + " FROM"
            + " t_maker_return t1"
            + " LEFT JOIN t_item t2 ON t2.id = t1.part_no_id "
            + " WHERE"
            + " t1.voucher_number = :voucherNumber "
            + " AND t1.order_id = :orderId "
            + " ORDER BY  "
            + " color_code ASC, sort_order ASC", nativeQuery = true)
    Page<ExtendedTMakerReturnPDFEntity> getMakerReturnInfo(
            @Param("voucherNumber") String voucherNumber,
            @Param("orderId") BigInteger orderId,
            Pageable pageable);

}
