package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTagdatEntity;

/**
 *
 * ExtendedTFOrderRepository.
 *
 */
@Repository
public interface ExtendedTagdatRepository extends JpaRepository<ExtendedTagdatEntity, String> {
    /**
     * TAGDAT登録用品番データ を検索する.
     * @param pageable {@link Pageable}
     * @return TAGDAT登録用品番データを取得する
     */
    @Query(value = " SELECT "
    	// PRD_0149 mod JFE start
    		//+ "     t1.id AS id "
    	    + "     CONCAT(CAST(t1.id AS char), t2.color_code, t2.size, IFNULL(t2.jan_code, '')) AS id "
    	// PRD_0149 mod JFE end
            + "   , cast(CURDATE() + 0 as char) AS crtymd "
            + "   , t1.brand_code AS brkg "
            + "   , t1.year AS datrec "
            + "   , t1.season_code AS season "
            + "   , t1.part_no AS partno "
            + "   , t1.retail_price AS jodai "
            + "   , 999999 AS hacno "
            + "   , t2.color_code AS iro "
            + "   , t2.size AS szkg "
            + "   , t2.jan_code AS jan "
            + "   , t1.reduced_tax_rate_flg AS taxflg"
            + "   , cast(CURTIME() + 0 as char) AS crthms "
            + "   , CASE WHEN t1.tagdat_created_at IS NULL THEN '1' ELSE '2' END AS syubt "
            + " FROM "
            + "   t_item t1 "
            + " LEFT JOIN t_sku t2 ON t2.deleted_at IS NULL AND t2.part_no_id = t1.id "
            + " WHERE "
            + "       t1.deleted_at IS NULL  "
            + "   AND t1.tagdat_created_flg = '0' "
            + " ORDER BY t1.id ASC ", nativeQuery = true)
    Page<ExtendedTagdatEntity> findItem(Pageable pageable);

    /**
     * TAGDAT登録用発注データ を検索する.
     * @param pageable {@link Pageable}
     * @return TAGDAT登録用発注データを取得する
     */
    @Query(value = " SELECT "
    	// PRD_0149 mod JFE start
    		//+ "     t3.id AS id "
    	    + "     CONCAT(CAST(t3.id AS char), t2.color_code, t2.size, IFNULL(t2.jan_code, '')) AS id "
    	// PRD_0149 mod JFE end
            + "   , cast(CURDATE() + 0 as char) AS crtymd "
            + "   , t1.brand_code AS brkg "
            + "   , t1.year AS datrec "
            + "   , t1.season_code AS season "
            + "   , t3.part_no AS partno "
            + "   , t3.retail_price AS jodai "
            + "   , t3.order_number AS hacno "
            + "   , t2.color_code AS iro "
            + "   , t2.size AS szkg "
            + "   , t2.jan_code AS jan "
            + "   , t1.reduced_tax_rate_flg AS taxflg"
            + "   , cast(CURTIME() + 0 as char) AS crthms "
            + "   , CASE WHEN t3.tagdat_created_at IS NULL THEN '1' ELSE '2' END AS syubt "
            + " FROM "
            + "   t_order t3 "
            + " LEFT JOIN t_item t1 ON t1.deleted_at IS NULL AND t1.id = t3.part_no_id "
            + " LEFT JOIN t_sku t2 ON t2.deleted_at IS NULL AND t2.part_no_id = t1.id "
            + " WHERE "
            + "       t3.deleted_at IS NULL  "
            + "   AND t3.tagdat_created_flg = '0' "
            + " ORDER BY t3.part_no_id ASC ", nativeQuery = true)
    Page<ExtendedTagdatEntity> findOrder(Pageable pageable);

}
