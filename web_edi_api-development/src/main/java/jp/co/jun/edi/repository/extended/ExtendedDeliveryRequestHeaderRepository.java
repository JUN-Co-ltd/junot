package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedDeliveryRequestPdfHeaderEntity;

/**
 *
 * ExtendedDeliveryRequestHeaderRepository.
 *
 */
@Repository
public interface ExtendedDeliveryRequestHeaderRepository extends JpaRepository<ExtendedDeliveryRequestPdfHeaderEntity, BigInteger> {
    /**
     * <pre>
     * SELECT
     *   t1.id AS delivery_id
     * , t1.order_number AS order_number -- 発注No.
     * , t1.delivery_count AS delivery_count -- 回数
     * , t3.mdf_maker_code AS mdf_maker_code -- 取引先コード
     * , (  SELECT sub1.name FROM m_sirmst sub1
     *      WHERE sub1.sire = t3.mdf_maker_code
     *      AND sub1.deleted_at IS NULL) AS name -- 取引先名称
     * , t2.part_no AS part_no -- 品番
     * , t2.product_name AS product_name -- 品名
     * , t3.retail_price AS retail_price -- 上代
     * , t3.unit_price AS unit_price -- 単価
     * , (  SELECT sub3.item2 FROM m_codmst sub2
     *      LEFT JOIN m_codmst sub3 ON sub3.deleted_at IS NULL AND sub3.tblid='61' AND sub3.code1 = sub2.item3
     *      WHERE sub2.deleted_at IS NULL
     *       AND sub2.tblid='02'
     *       AND sub2.code1 = t2.brand_code) AS  company_name -- 会社名
     * , (  SELECT sub4.item8 FROM m_codmst sub4
     *      WHERE sub4.deleted_at IS NULL
     *      AND sub4.tblid = '02'
     *      AND sub4.code1 = t2.brand_code) AS brand_name -- ブランド名
     * FROM t_delivery t1
     * LEFT OUTER JOIN t_item t2 ON t1.part_no_id = t2.id AND t2.deleted_at IS NULL
     * LEFT OUTER JOIN t_order t3 ON t1.order_id = t3.id AND t3.deleted_at IS NULL
     * WHERE t1.deleted_at IS NULL
     * AND t1.id = 1
     * </pre>
     * @param deliveryId 納品ID
     * @return 納品依頼メール送信のヘッダー情報を取得する
     */
    @Query(value = "SELECT"
            + "   t1.id AS delivery_id"
            + " , t1.order_number AS order_number"
            + " , t1.delivery_count AS delivery_count"
            + " , t1.non_conforming_product_type AS non_conforming_product_type"
            + " , t1.non_conforming_product_unit_price AS non_conforming_product_unit_price"
            + " , t3.mdf_maker_code AS mdf_maker_code"
            + " , (  SELECT sub1.name FROM m_sirmst sub1"
            + "      WHERE sub1.sire = t3.mdf_maker_code"
            + "      AND sub1.deleted_at IS NULL) AS name"
            + " , t2.part_no AS part_no"
            + " , t2.product_name AS product_name"
            + " , t3.retail_price AS retail_price"
            + " , t3.unit_price AS unit_price"
            + " , (  SELECT sub3.item2 "
            + "      FROM m_codmst sub2"
            + "      LEFT JOIN m_codmst sub3 ON sub3.deleted_at IS NULL AND sub3.tblid='61' AND sub3.code1 = sub2.item3 AND sub3.mntflg IN ('1', '2', '')"
            + "      WHERE sub2.deleted_at IS NULL"
            + "      AND sub2.tblid='02'"
            + "      AND sub2.code1 = t2.brand_code"
            + "      AND sub2.mntflg IN ('1', '2', '')) AS  company_name"
            + " , (  SELECT sub4.item8 "
            + "      FROM m_codmst sub4"
            + "      WHERE sub4.deleted_at IS NULL"
            + "      AND sub4.tblid = '02'"
            + "      AND sub4.code1 = t2.brand_code"
            + "      AND sub4.mntflg IN ('1', '2', '')) AS brand_name"
            + " ,t2.brand_code"
            + " ,t2.item_code"
            + " FROM t_delivery t1"
            + " INNER JOIN t_item t2 ON t1.part_no_id = t2.id AND t2.deleted_at IS NULL"
            + " INNER JOIN t_order t3 ON t1.order_id = t3.id AND t3.deleted_at IS NULL"
            + " WHERE t1.deleted_at IS NULL"
            + " AND t1.id = :deliveryId", nativeQuery = true)
    Optional<ExtendedDeliveryRequestPdfHeaderEntity> findByDeliveryId(@Param("deliveryId") BigInteger deliveryId);

}
