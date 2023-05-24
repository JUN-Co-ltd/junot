package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnEntity;

/**
 * ExtendedTMakerReturnRepository.
 */
@Repository
public interface ExtendedTMakerReturnRepository extends JpaRepository<ExtendedTMakerReturnEntity, BigInteger> {

    /**
     * 伝票番号と発注IDをキーにメーカー返品情報リストを検索する.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return メーカー返品情報リスト
     */
    @Query(value = "SELECT"
            + "       mr.*"
            + "       , t.name shop_name"
            + "       , sr.name supplier_name"
            + "       , o.product_order_at"
            + "       , o.retail_price"
            + "       , o.unit_price"
            + "       , i.product_name"
            + "       , i.brand_code"
            + "       , i.item_code"
            + "       , i.other_cost"
            + "       , f.file_no_id maker_return_file_no_id"
            + "       , ss.stock_lot"
            + "       , mc.item2 as mdf_staff_name"
            + "     FROM"
            + "       t_maker_return mr"
            + "       LEFT OUTER JOIN t_order o"
            + "         ON o.id = mr.order_id"
            + "         AND o.deleted_at IS NULL"
            + "       LEFT OUTER JOIN t_item i"
            + "         ON i.id = mr.part_no_id"
            + "         AND i.deleted_at IS NULL"
            + "       LEFT OUTER JOIN t_maker_return_file_info f"
            + "         ON f.voucher_number = mr.voucher_number"
            + "         AND f.order_id = mr.order_id"
            + "         AND f.deleted_at IS NULL"
            + "       LEFT OUTER JOIN m_sirmst sr"
            + "         ON sr.mntflg IN ('1','2',' ')"
            + "         AND sr.deleted_at IS NULL"
            + "         AND sr.sire = mr.supplier_code"
            + "       LEFT OUTER JOIN t_shop_stock ss"
            + "         ON ss.shop_code = mr.shpcd"
            + "         AND ss.part_no = mr.part_no"
            + "         AND ss.color_code = mr.color_code"
            + "         AND ss.size = mr.size"
            + "         AND ss.deleted_at IS NULL"
            + "       LEFT OUTER JOIN m_tnpmst t"
            + "         ON t.shpcd = mr.shpcd"
            + "         AND t.deletedtype = false"
            + "         AND t.deleted_at IS NULL"
            + "       LEFT OUTER JOIN m_codmst mc"
            + "         ON mc.code1 = mr.mdf_staff_code"
            + "         AND mc.tblid = '22'"
            + "         AND mc.deleted_at IS NULL"
            + "     WHERE"
            + "       mr.voucher_number = :voucherNumber"
            + "       AND mr.order_id = :orderId"
            + "       AND mr.deleted_at IS NULL", nativeQuery = true)
    Optional<List<ExtendedTMakerReturnEntity>> findByVoucherNumberAndOrderId(
            @Param("voucherNumber") String voucherNumber,
            @Param("orderId") BigInteger orderId);
}
