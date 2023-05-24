package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TPurchaseDigestionItemPDFEntity;

//PRD_0134 #10654 add JEF start
@Repository
public interface TPurchaseDigestionItemPdfRepository extends JpaRepository<TPurchaseDigestionItemPDFEntity, BigInteger> {
	  /**
     * 仕入情報などから、PDF作成に必要な情報を取得する
     * @param yyyyMMfrom
     * @param yyyyMMto
     * @param pageable Pageable
     * @return 仕入ファイル情報
     */
    @Query(value = "SELECT tp.id AS id,"
    		+ " tp.arrival_at AS arrival_at,"
    		+ " tp.division_code AS division_code,"
    		+ " tp.purchase_voucher_number AS purchase_voucher_number,"
    		+ " tp.purchase_voucher_line AS purchase_voucher_line,"
    		+ " tp.supplier_code AS supplier_code,"
    		+ " tp.part_no AS part_no,"
    		+ " tp.fix_arrival_count AS fix_arrival_count,"
    		+ " tp.purchase_unit_price AS purchase_unit_price,"
    		+ " tp.size AS size,"
    		+ " tp.color_code AS color_code,"
    		+ " tp.arrival_count AS arrival_count,"
    		+ " tp.purchase_type AS purchase_type,"
    		+ " tp.arrival_place AS arrival_place,"
    		+ " tit.retail_price AS retail_price,"
    		+ " tp.mdf_maker_factory_code AS mdf_maker_factory_code,"
    		+ " '' AS order_number,"
    		+ " '' AS all_completion_type,"
    		+ " '' AS purchase_voucher_type,"
    		+ " (IFNULL(tit.matl_cost,0) + IFNULL(tit.processing_cost,0) + IFNULL(tit.accessories_cost,0) + IFNULL(tit.other_cost,0)) AS unit_price ,"
    		+ " sir.yubin AS yubin,"
    		+ " sir.add1 AS add1,"
    		+ " sir.add2 AS add2,"
    		+ " sir.add3 AS add3,"
    		+ " sir.name AS name,"
    		+ " sir.sirkbn AS sirkbn,"
    		+ " tit.product_name AS product_name,"
    		+ " tit.dept_code AS dept_code,"
    		+ " (SELECT sq1.item2 FROM m_codmst sq1"
    		+ " WHERE sq1.deleted_at IS NULL"
    		+ " AND sq1.mntflg IN ('1','2','')"
    		+ " AND sq1.tblid = '10'"
    		+ " AND sq1.code1 = tp.color_code) AS color_code_name,"
    		+ " (SELECT sq2.item2 FROM m_codmst sq1"
    		+ " LEFT JOIN m_codmst sq2 ON sq2.deleted_at IS NULL"
    		+ " AND sq2.mntflg IN ('1' , '2', '')"
    		+ " AND sq2.tblid = '61'"
    		+ " AND sq2.code1 = sq1.item3"
    		+ " WHERE sq1.deleted_at IS NULL"
    		+ " AND sq1.mntflg IN ('1' , '2', '') "
    		+ " AND sq1.tblid = '02'"
    		+ " AND sq1.code1 = tit.brand_code) AS company_name,"
    		+ " (SELECT sq.item1 FROM m_codmst sq"
    		+ " WHERE sq.deleted_at IS NULL"
    		+ " AND sq.mntflg IN ('1' , '2', '')"
    		+ " AND sq.tblid = '02'"
    		+ " AND sq.code1 = tit.brand_code) AS brand_name,"
    		+ " (SELECT sq1.item1 FROM m_codmst sq1"
    		+ " WHERE sq1.deleted_at IS NULL"
    		+ " AND sq1.mntflg IN ('1' , '2', '')"
    		+ " AND sq1.tblid = '03'"
    		+ " AND sq1.code1 = tit.brand_code"
    		+ " AND sq1.code2 = tit.item_code) AS item_name,"
    		+ " CAST((SELECT sq2.jun FROM m_sizmst sq2"
    		+ " WHERE sq2.deleted_at IS NULL"
    		+ " AND sq2.mntflg IN ('1','2','')"
    		+ " AND sq2.hscd = CONCAT(tit.brand_code,tit.item_code)"
    		+ " AND sq2.szkg = tp.size) AS SIGNED)jun"
    		+ " FROM t_purchase tp"
    		+ " INNER JOIN m_sirmst sir ON tp.supplier_code = sir.sire"
    		+ " INNER JOIN t_item tit ON tp.part_no_id = tit.id"
    		+ " INNER JOIN t_purchases_voucher tpv ON tp.purchase_voucher_number = tpv.purchase_voucher_number"
    		+ " INNER JOIN t_purchase_file_info tpfi ON tpfi.purchase_voucher_number = tp.purchase_voucher_number"
    		+ " AND tpfi.created_at BETWEEN :yyyyMMfrom AND :yyyyMMto"
    		+ " AND tpfi.status = 0"
    		+ " AND ((tp.purchase_type = 9 AND tp.arrival_place = 19)OR(tp.purchase_type = 3 AND tp.arrival_place = 19))"
            + " AND tp.deleted_at IS NULL" , nativeQuery = true)
    List<TPurchaseDigestionItemPDFEntity> findByPurchaseDigestionItem(
             @Param("yyyyMMfrom") String yyyyMMfrom,@Param("yyyyMMto") String yyyyMMto, Pageable pageable);


}
//PRD_0134 #10654 add JEF end