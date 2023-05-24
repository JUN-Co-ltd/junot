package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
//PRD_0179 #10654 add JEF start
import java.util.Date;
//PRD_0179 #10654 add JEF end
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTPurchaseHeadPDFEntity;

//PRD_0134 #10654 add JEF start
/**
 * ExtendedTPurchaseHeadPDFRepository.
 */
@Repository
public interface ExtendedTPurchaseHeadPDFRepository extends JpaRepository<ExtendedTPurchaseHeadPDFEntity, BigInteger> {
    /**
     * 仕入明細HeadPDF作成用SQL.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return 仕入明細HeadPDF情報を取得する
     */
    @Query(value = "  SELECT"
            + "  t1.id as id" // ID
            // PRD_0179 #10654 mod JEF start
            //+ ", t1.arrival_at as arrival_at" // 入荷日
            + ", CASE WHEN t1.purchase_type = 3 THEN :createdAt"
            + "  ELSE t1.arrival_at END as arrival_at" // 入荷日
            // PRD_0179 #10654 mod JEF end
            + ", t1.division_code as division_code" // 課コード
            + ", t1.purchase_voucher_number as purchase_voucher_number" // 伝票番号
            + ", t1.purchase_type as purchase_type" // 仕入区分
            + ", t1.arrival_place as arrival_place" // 入荷場所
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
            //PRD_0199 JFE add start
            + ", t2.sirkbn as sirkbn" // 仕入先マスタ.仕入先区分
            + ", t4.id as order_id" // 発注ID
            + ", t4.expense_item as expense_item" // 費目
            + ", t1.purchase_count as purchase_count" //引取回数
            //PRD_0199 JFE add end
            + "  FROM"
            + "  t_purchase t1"
            + "  LEFT JOIN m_sirmst t2 ON t2.sire = t1.supplier_code"
            + "  LEFT JOIN t_item t3 ON t3.id = t1.part_no_id "
            //PRD_0199 JFE add start
            + "  LEFT JOIN t_order t4 ON t4.id = t1.order_id "
            //PRD_0199 JFE add end
            + "  WHERE"
            + "  t1.purchase_voucher_number = :purchaseVoucherNumber "
            + "  AND t1.purchase_voucher_line = 1 "
            + "  AND t1.order_id = :orderId "
            + " ORDER BY"
            + "  t1.id", nativeQuery = true)
    Optional<ExtendedTPurchaseHeadPDFEntity> getPurchaseHeadInfo(
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            // PRD_0179 #10654 mod JEF start
            //@Param("orderId") BigInteger orderId);
            @Param("orderId") BigInteger orderId,
            @Param("createdAt") Date createdAt);
            // PRD_0179 #10654 mod JEF end
}
//PRD_0134 #10654 add JEF end