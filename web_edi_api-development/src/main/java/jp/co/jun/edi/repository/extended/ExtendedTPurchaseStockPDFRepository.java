package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTPurchaseStockPDFEntity;

//PRD_0134 #10654 add JEF start
/**
 * ExtendedTPurchaseStockPDFRepository.
 */
@Repository
public interface ExtendedTPurchaseStockPDFRepository extends JpaRepository<ExtendedTPurchaseStockPDFEntity, BigInteger> {
    /**
     * 仕入明細PDF作成用SQL.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return 仕入明細PDF情報を取得する
     */
    @Query(value = " SELECT "
            + "  t1.id as id "
            + "  , '' as stock_code " // 入荷先コード
            + "  , '' AS stock_name" // 入荷先名
            + "  , t2.retail_price as retail_price " // 上代
            + "  , '' as request_number " // 依頼No
            + "  , t2.order_number as order_number " // 発注No
            //PRD_0194 JFE mod start
//            + "  , t2.all_completion_type as all_completion_type " // 全済区分:9:未　0:済
            + "  , IFNULL(t2.all_completion_type,0) as all_completion_type " // 全済区分:9:未　0:済
            //PRD_0194 JFE mod end
            + "  , t3.dept_code as division_code " // 部門コード
            + "  , (SELECT sq.item1 FROM m_codmst sq "
            + "       WHERE sq.deleted_at IS NULL "
            + "       AND sq.mntflg IN ('1', '2', '') "
            + "       AND sq.tblid='02' "
            + "       AND sq.code1 = t3.brand_code ) as brand_name " // ブランド名
            + "  , (SELECT sq1.item1 FROM m_codmst sq1 "
            + "     WHERE sq1.deleted_at IS NULL "
            + "     AND sq1.mntflg IN ('1', '2', '') "
            + "     AND sq1.tblid='03' "
            + "     AND sq1.code1 = t3.brand_code "
            + "     AND sq1.code2 = t3.item_code) as item_name " // アイテム名
            // PRD_0179 #10654 mod JEF start
            //+ "  ,SUM(t1.fix_arrival_count) as quantity " // 数量
            + "  ,CASE WHEN t1.purchase_type = 3 THEN SUM(t1.fix_arrival_count * (-1))"
            + "   ELSE SUM(t1.fix_arrival_count) END as quantity " // 数量
            // PRD_0179 #10654 mod JEF end
            + "  ,t1.purchase_unit_price as purchase_unit_price " // 仕入単価
            + "  ,t2.unit_price as unit_price " // 発注単価
            + "  ,t2.non_conforming_product_unit_price as non_conforming_product_unit_price" //B級品単価
            + "  FROM t_purchase t1 "
            + "  LEFT JOIN t_order t2 ON t2.id = t1.order_id "
            + "  LEFT JOIN t_item t3 ON t3.id = t1.part_no_id "
            + "  WHERE "
            + "   t1.purchase_voucher_number = :purchaseVoucherNumber "
            + "   AND t1.order_id = :orderId ", nativeQuery = true)
    Optional<ExtendedTPurchaseStockPDFEntity> getPurchaseStockInfo(
            @Param("purchaseVoucherNumber") String purchaseVoucherNumber,
            @Param("orderId") BigInteger orderId);
}
//PRD_0134 #10654 add JEF end