package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnHeadPDFEntity;

/**
 * ExtendedTMakerReturnHeadPDFRepository.
 */
@Repository
public interface ExtendedTMakerReturnHeadPDFRepository extends JpaRepository<ExtendedTMakerReturnHeadPDFEntity, BigInteger> {
    /**
     * 返品明細HeadPDF作成用SQL.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return 返品明細HeadPDF情報を取得する
     */
    @Query(value = "  SELECT"
            + "  t1.id as id" // ID
            // PRD_0073 mod SIT start
            //+ ", t1.return_at as return_at" // 返金日
            + ", :createdAt as return_at" // 返金日
            // PRD_0073 mod SIT end
            + ", '' as division_code" // 課コード
            + ", t1.voucher_number as voucher_number" // 伝票番号
            + ", t2.yubin as yubin" // 郵便番号
            + ", t2.add1 as address1" // 住所1
            + ", t2.add2 as addreess2" // 住所2
            + ", t2.add3 as addreess3" // 住所3
            + ", t2.sire as sire" // 仕入先コード
            + ", t2.name as send_to_name" // 送付先名
            + ", (SELECT sq2.item2 FROM m_codmst sq1" // 会社名
            + "     LEFT JOIN m_codmst sq2 "
            + "       ON sq2.deleted_at IS NULL "
            + "       AND sq2.mntflg IN ('1', '2', '')"
            + "       AND sq2.tblid='61' "
            + "       AND sq2.code1 = sq1.item3 "
            + "     WHERE sq1.deleted_at IS NULL "
            + "     AND sq1.mntflg IN ('1', '2', '')"
            + "     AND sq1.tblid='02' "
            + "     AND sq1.code1 = t3.brand_code ) as company_name"
            + "     , (SELECT sq.item1 FROM m_codmst sq" // ブランド名
            + "             WHERE sq.deleted_at IS NULL"
            + "             AND sq.mntflg IN ('1', '2', '')"
            + "             AND sq.tblid='02' "
            + "             AND sq.code1 = t3.brand_code ) as brand_name"
            + ", t1.part_no as part_no" // 品番
            + ", t3.product_name as product_name" // 品名
            + "  FROM"
            + "  t_maker_return t1"
            + "  LEFT JOIN m_sirmst t2 ON t2.sire = t1.supplier_code"
            + "  LEFT JOIN t_item t3 ON t3.id = t1.part_no_id "
            + "  WHERE"
            + "  t1.voucher_number = :voucherNumber "
            + "  AND t1.voucher_line = 1 "
            + "  AND t1.order_id = :orderId "
            + " ORDER BY"
            + "  t1.id", nativeQuery = true)
    Optional<ExtendedTMakerReturnHeadPDFEntity> getMakerReturnHeadInfo(
            @Param("voucherNumber") String voucherNumber,
            // PRD_0073 mod SIT start
            //@Param("orderId") BigInteger orderId);
            @Param("orderId") BigInteger orderId,
            @Param("createdAt") Date createdAt);
            // PRD_0073 mod SIT end
}
