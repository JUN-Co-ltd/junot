package jp.co.jun.edi.repository.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.component.DeliverySearchListComponent;
import jp.co.jun.edi.entity.TDeliverySearchResultEntity;
import jp.co.jun.edi.model.DeliverySearchListConditionModel;
import jp.co.jun.edi.repository.custom.TDeliverySearchListRepositoryCustom;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.DeliveryListAllocationStatusType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 配分一覧Repository実装クラス.
 */
public class TDeliverySearchListRepositoryImpl implements TDeliverySearchListRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DeliverySearchListComponent deliverySearchListComponent;

    private static final String SELECT_PHRASE_SQL =
            "  SELECT d.id AS delivery_id"
            + "     , d.order_id AS order_id"
            + "     , d.order_number AS order_number"
            + "     , d.part_no_id AS part_no_id"
            + "     , d.part_no AS part_no"
            + "     , dd.store_registered_flg AS store_registered_flg"
            + "     , i.product_name as product_name"
            + "     , d.delivery_approve_status AS delivery_approve_status"
            + "     , d.delivery_count AS delivery_count"
            + "     , dd.correction_at AS correction_at"
            // PRD_0087 mod SIT start
            //+ "     , dd.allocation_confirm_flg AS allocation_confirm_flg"
            + "     , dd.allocation_complete_at AS allocation_complete_at"
            + "     , dd.allocation_record_at AS allocation_record_at"
            // PRD_0087 mod SIT end
            + "     , dd.arrival_flg AS arrival_flg"
            + "     , dd.shipping_instructions_flg AS shipping_instructions_flg"
            //PRD_0127 #9837 add JFE start
			+ "     , left(mdl2.company_name,2) AS company_name"
            //PRD_0127 #9837 add JFE end
            + "     , o.quantity AS quantity"
            + "     , o.product_complete_order AS product_complete_order"
            //PRD_0113 #7411 add JFE start
//            // PRD_0008 mod SIT start
//            //+ "     , o.all_completion_type AS all_completion_type";
//            + "     , o.all_completion_type AS all_completion_type"
//            + "     , COALESCE(SUM(tl.transaction_lot), 0) AS transaction_lot"
//            + "     , COALESCE(al.allocation_lot, 0) AS allocation_lot"
//            + "     , COALESCE(fac.fix_arrival_count, 0) AS fix_arrival_count";
//            // PRD_0008 mod SIT end
            + "     , o.all_completion_type AS all_completion_type"
            + "     , COALESCE(SUM(tl.transaction_lot), 0) AS transaction_lot"
            //+ "     , COALESCE(al.allocation_lot, 0) AS allocation_lot"
            + "     , COALESCE((SELECT SUM(dss1.delivery_lot)AS delivery_lot"
            + "    		FROM t_delivery_detail dd1"
            + "    			LEFT JOIN t_delivery_store dst1"
            + "      			ON dst1.delivery_detail_id = dd1.id"
            + "      		LEFT JOIN t_delivery_store_sku dss1"
            + "      			ON dst1.id = dss1.delivery_store_id"
            + "      	WHERE dd.delivery_id = dd1.delivery_id"
            + "      		AND dst1.deleted_at is NULL"
            + "      		AND dss1.deleted_at is NULL"
            + "      	GROUP BY dd1.delivery_id"
            + "      ),0) AS allocation_lot"
            + "     , COALESCE(fac.fix_arrival_count, 0) AS fix_arrival_count";
    		//PRD_0113 #7411 add JFE end

    private static final String TRANSACTION_LOT_SUB_QUERY =
            "       ("
            // PRD_0008 mod SIT start
            //+ "      SELECT SUM(ds.delivery_lot) AS transaction_lot"
            //+ "        FROM t_delivery_detail dd"
            //+ "          INNER JOIN t_delivery_sku ds"
            //+ "                  ON dd.id = ds.delivery_detail_id"
            //+ "                 AND ds.deleted_at IS NULL"
            //+ "       WHERE dd.delivery_id = d.id"
            //+ "         AND dd.deleted_at IS NULL"
            //+ "     ) AS transaction_lot";
            + "      SELECT SUM(ds.delivery_lot) AS transaction_lot"
            + "      , ds.delivery_detail_id"
            + "        FROM t_delivery_sku ds"
            + "       WHERE ds.deleted_at IS NULL"
            + "       GROUP BY ds.delivery_detail_id"
            + "     )";
            // PRD_0008 mod SIT end

    //PRD_0113 #7411  JFE del start
//    private static final String ALLOCATION_LOT_SUB_QUERY =
//            "       ("
//            // PRD_0008 mod SIT start
//            //+ "      SELECT COALESCE(SUM(dss.delivery_lot), 0) AS allocation_lot"
//            //+ "      , dd.delivery_id"
//            //+ "        FROM t_delivery_detail dd"
//            //+ "          LEFT OUTER JOIN t_delivery_store dst"
//            //+ "                       ON dd.id = dst.delivery_detail_id"
//            //+ "                      AND dst.deleted_at IS NULL"
//            //+ "          LEFT OUTER JOIN t_delivery_store_sku dss"
//            //+ "                       ON dst.id = dss.delivery_store_id"
//            //+ "                      AND dss.deleted_at IS NULL"
//            //+ "       WHERE dd.delivery_id = d.id"
//            //+ "         AND dd.deleted_at IS NULL"
//            + "      SELECT"
//            + "        COALESCE(SUM(dstw.delivery_lot), 0) AS allocation_lot"
//            + "      , dd.delivery_id"
//            + "        FROM t_delivery_detail dd"
//            + "          LEFT OUTER JOIN ("
//            + "            SELECT"
//            + "              SUM(dss.delivery_lot) AS delivery_lot"
//            + "            , dst.delivery_detail_id"
//            + "              FROM t_delivery_store dst"
//            + "                LEFT OUTER JOIN t_delivery_store_sku dss"
//            + "                             ON dst.id = dss.delivery_store_id"
//            + "                            AND dss.deleted_at IS NULL"
//            + "            WHERE dst.deleted_at IS NULL"
//            + "            GROUP BY dst.delivery_detail_id"
//            + "                       ) dstw"
//            + "                       ON dd.id = dstw.delivery_detail_id"
//            + "        WHERE dd.deleted_at IS NULL"
//            + "        GROUP BY dd.delivery_id"
//            // PRD_0008 mod SIT end
//            + "     )";
    //PRD_0113 #7411  JFE del end

    private static final String GROUP_BY_PHRASE_SQL =
            "  GROUP BY d.id";

    private static final String ORDER_BY_PHRASE_SQL =
            // PRD_0008 mod SIT start
            //"  ORDER BY correction_at DESC"
            //+ "       , order_number ASC"
            //+ "       , delivery_count ASC"
            //+ "       , delivery_id ASC";
            // PRD_0038 mod SIT start
            //"  ORDER BY dd.correction_at DESC"
              "  ORDER BY dd.correction_at ASC "
            // PRD_0038 mod SIT end
            + "       , d.order_number ASC"
            + "       , d.delivery_count ASC"
            + "       , dd.delivery_id ASC";
            // PRD_0008 mod SIT end


    private static final String FIX_ARRIVAL_COUNT_SQL =
            "       ("
            + "      SELECT COALESCE(SUM(fix_arrival_count), 0) AS fix_arrival_count"
            // PRD_0003 add SIT start
            + "      , p.delivery_id"
            // PRD_0003 add SIT end
            + "         FROM t_purchase p"
            // PRD_0003 mod SIT start
            //+ "             INNER JOIN t_delivery d"
            //+ "                 ON p.delivery_id = d.id"
            //+ "      WHERE p.delivery_id = d.id"
            + "      WHERE p.deleted_at IS NULL"
            + "      GROUP BY p.delivery_id"
            // PRD_0003 mod SIT end
            + "     )";

    // 納品依頼ID件数取得
    public static final String SELECT_CNT_PHRASE_SQL = "SELECT COUNT(DISTINCT d.id) ";

    // 採番No桁数
    public static final int NUMBER_DIGIT = 6;

    /**
     * SELECT d.id AS delivery_id
     *       ,d.order_id AS order_id
     *       ,d.order_number AS order_number
     *       ,d.part_no_id AS part_no_id
     *       ,d.part_no AS part_no
     *       ,d.store_registered_flg AS store_registered_flg
     *       ,i.product_name AS product_name
     *       ,d.delivery_approve_status AS delivery_approve_status
     *       ,d.delivery_count AS delivery_count
     *       ,dd.correction_at AS correction_at
     *       ,dd.allocation_complete_flg AS allocation_complete_flg
     *       ,dd.allocation_complete_at AS allocation_complete_at
     *       ,dd.allocation_record_at AS allocation_record_at
     *       ,dd.arrival_flg AS arrival_flg
     *       ,dd.shipping_instructions_flg AS shipping_instructions_flg
     *       ,o.quantity AS quantity
     *       ,o.product_complete_order AS product_complete_order
     *       ,o.all_completion_type AS all_completion_type
     *       ,left(mdl.company_name,2) AS company_name
     *       ,COALESCE(SUM(tl.transaction_lot), 0) AS transaction_lot
     *       ,COALESCE(al.allocation_lot, 0) AS allocation_lot
     *       ,COALESCE(fac.fix_arrival_count, 0) AS fix_arrival_count
     * FROM t_delivery d
     * INNER JOIN t_delivery_detail dd
     *     ON d.id = dd.delivery_id
     *     AND dd.deleted_at IS NULL
     *     AND dd.correction_at >= ?
     *     AND dd.correction_at <= ?
     *     AND d.order_number >= ?
     *     AND d.order_number <= ?
     *     AND dd.delivery_number >= ?
     *     AND dd.delivery_number <= ?
     *     AND dd.delivery_request_number >= ?
     *     AND dd.delivery_request_number <= ?
     *     AND dd.carry_type = ?
     * INNER JOIN t_order o
     *     ON d.order_id = o.id
     *     AND o.deleted_at IS NULL
     *     AND o.mdf_maker_code = ?
     *     AND (o.product_complete_order IN ('5','6') OR o.all_completion_type = '0') -- 完納
     *     // AND (o.product_complete_order IN ('0',NULL) AND o.all_completion_type IN ('9',NULL)) ※未完納
     * INNER JOIN t_item i
     *     ON d.part_no_id = i.id
     *     AND i.deleted_at IS NULL
     * LEFT OUTER JOIN m_codmst m
     *     ON i.brand_code = m.code1
     *     AND tblid = '02'
     *     AND m.mntflg IN ('1', '2', '')
     *     AND m.deleted_at IS NULL
     * INNER JOIN m_delivery_destination mdd
     *     ON dd.logistics_code = mdd.logistics_code
     * INNER JOIN m_delivery_location mdl
     *     ON mdd.delivery_location_id = mdl.id
     * LEFT OUTER JOIN (
     *                  SELECT SUM(ds.delivery_lot) AS transaction_lot
     *                        ,ds.delivery_detail_id
     *                  FROM t_delivery_sku ds
     *                  WHERE ds.deleted_at IS NULL
     *                  GROUP BY ds.delivery_detail_id) tl
     *     ON dd.id = tl.delivery_detail_id
     * LEFT OUTER JOIN (
     *                  SELECT COALESCE(SUM(dstw.delivery_lot), 0) AS allocation_lot
     *                        ,dd.delivery_id
     *                  FROM t_delivery_detail dd
     *                  LEFT OUTER JOIN (
     *                                   SELECT SUM(dss.delivery_lot) AS delivery_lot
     *                                         ,dst.delivery_detail_id
     *                                   FROM t_delivery_store dst
     *                                   LEFT OUTER JOIN t_delivery_store_sku dss
     *                                       ON dst.id = dss.delivery_store_id
     *                                       AND dss.deleted_at IS NULL
     *                                   WHERE dst.deleted_at IS NULL
     *                                   GROUP BY dst.delivery_detail_id) dstw
     *                      ON dd.id = dstw.delivery_detail_id
     *                  WHERE dd.deleted_at IS NULL
     *                  GROUP BY dd.delivery_id) al
     *     ON d.id = al.delivery_id
     * LEFT OUTER JOIN (
     *                  SELECT COALESCE(SUM(fix_arrival_count), 0) AS fix_arrival_count
     *                        ,p.delivery_id
     *                  FROM t_purchase p
     *                  WHERE p.deleted_at IS NULL
     *                  GROUP BY p.delivery_id) fac
     *     ON d.id = fac.delivery_id
     * WHERE d.deleted_at IS NULL
     *     AND d.part_no LIKE :firstPartNo
     *     OR d.part_no LIKE :partNo0
     *     OR d.part_no LIKE :partNo1
     *     ...
     *     OR d.part_no LIKE :partNo(キーワード数-1) ※部分一致、複数検索
     *     OR i.brand_code IN (:brandCode)
     *
     *  // AND d.part_no LIKE :firstPartNo
     *  // OR d.part_no LIKE :partNo0
     *  // OR d.part_no LIKE :partNo1
     *  // ...
     *  // OR d.part_no LIKE :partNo(品番数-1) -- キーワード検索：品番 ※部分一致、複数検索
     *  // AND i.brand_code IN (:brandCode) -- キーワード検索：ブランドコード
     *
     *     AND m.item4 = :departmentCode
     *     AND dd.arrival_flg = ?
     *     AND d.delivery_approve_status = ?
     *     AND dd.shipping_instructions_flg = ?
     *
     *     AND (dd.allocation_complete_flg = TRUE
     *     AND  dd.allocation_complete_at = IS NOT NULL
     *     AND  dd.allocation_record_at = IS NOT NULL) -- 出荷済
     *
     *  // AND (dd.allocation_complete_flg = FALSE
     *  // AND  dd.allocation_complete_at = IS NULL
     *  // AND  dd.allocation_record_at = IS NULL) -- 未出荷
     *
     *
                // 未配分（全店配分のみ、得意先未配分）

     *     AND dd.store_registered_flg = FALSE
     *     AND dd.arrival_flg = FALSE -- 未配分
     *

     *  // AND dd.store_registered_flg = TRUE -- 配分済

     *  // AND dd.store_registered_flg = TRUE
     *  // AND COALESCE(fac.fix_arrival_count, 0) < COALESCE(al.allocation_lot, 0) -- 要再配分

     *  // AND dd.store_registered_flg = FALSE
     *  // AND dd.arrival_flg = TRUE -- 入荷済未配分
     * GROUP BY d.id
     * ORDER BY correction_at ASC
     *         ,order_number ASC
     *         ,delivery_count ASC
     *         ,delivery_id ASC
     * .
     */
    @Override
    public Page<TDeliverySearchResultEntity> findBySpec(final DeliverySearchListConditionModel searchCondition) {

        // SELECT句を生成
        final StringBuilder searchSql = new StringBuilder();
        generateSelectPhrase(searchSql);

        // FROM句を生成
        final StringBuilder fromPhraseSql = new StringBuilder();
        generateFromPhrase(searchCondition, fromPhraseSql);
        // FROM句を追加
        searchSql.append(fromPhraseSql);

        final StringBuilder wherePhraseSql = new StringBuilder();
        generateWherePhrase(searchCondition, wherePhraseSql);
        searchSql.append(wherePhraseSql);

        searchSql.append(GROUP_BY_PHRASE_SQL);
        searchSql.append(ORDER_BY_PHRASE_SQL);

        final Query serachQuery = entityManager.createNativeQuery(searchSql.toString(), TDeliverySearchResultEntity.class);
        setQueryParameters(searchCondition, serachQuery);

        int pageIdx = searchCondition.getPage();
        int maxResults = searchCondition.getMaxResults();
        serachQuery.setFirstResult(pageIdx * maxResults);
        serachQuery.setMaxResults(maxResults);

        @SuppressWarnings("unchecked")
        List<TDeliverySearchResultEntity> rslt = serachQuery.getResultList();

        // 件数取得
        Integer total = countBySpec(searchCondition, fromPhraseSql, null);

        final PageRequest pageRequest = PageRequest.of(searchCondition.getPage(), searchCondition.getMaxResults());
        return new PageImpl<>(rslt, pageRequest, total);
    }

    /**
     * クエリにパラメータを設定する.
     * @param searchCondition 配分一覧検索条件
     * @param query クエリ
     */
    private void setQueryParameters(final DeliverySearchListConditionModel searchCondition, final Query query) {
        final List<String> partNoList = deliverySearchListComponent.splitPartialMatchConditions(searchCondition.getPartNo());
        final List<String> brandCodeList = deliverySearchListComponent.splitPerfectMatchConditions(searchCondition.getBrandCode());
        // PRD_0011 del SIT start
        //final List<String> departmentCodeList = deliverySearchListComponent.splitPerfectMatchConditions(searchCondition.getDepartmentCode());
        // PRD_0011 del SIT end

        final CarryType carryType = searchCondition.getCarryType();
        final String mdfMakerCode = searchCondition.getMdfMakerCode();

        final Date deliveryAtFrom = searchCondition.getDeliveryAtFrom();
        final Date deliveryAtTo = searchCondition.getDeliveryAtTo();
        final BigInteger orderNumberFrom = searchCondition.getOrderNumberFrom();
        final BigInteger orderNumberTo = searchCondition.getOrderNumberTo();
        final String deliveryNumberFrom = searchCondition.getDeliveryNumberFrom();
        final String deliveryNumberTo = searchCondition.getDeliveryNumberTo();
        final String deliveryRequestNumberFrom = searchCondition.getDeliveryRequestNumberFrom();
        final String deliveryRequestNumberTo = searchCondition.getDeliveryRequestNumberTo();
        // PRD_0011 add SIT start
        final String departmentCode = searchCondition.getDepartmentCode();
        // PRD_0011 add SIT end

        if (!partNoList.isEmpty()) {
            setQueryParametersByPartNo(partNoList, query);
        }
        if (!brandCodeList.isEmpty()) {
            query.setParameter("brandCode", brandCodeList);
        }
        // PRD_0011 del SIT start
        //if (!departmentCodeList.isEmpty()) {
        //    query.setParameter("departmentCode", departmentCodeList);
        //}
        if (!StringUtils.isEmpty(departmentCode)) {
            query.setParameter("departmentCode", departmentCode);
        }
        // PRD_0011 del SIT end
        if (!Objects.isNull(carryType)) {
            query.setParameter("carryType", carryType.getValue());
        }
        if (!StringUtils.isEmpty(mdfMakerCode)) {
            query.setParameter("mdfMakerCode", mdfMakerCode);
        }
        if (!Objects.isNull(deliveryAtFrom)) {
            query.setParameter("deliveryAtFrom", DateUtils.truncateDate(deliveryAtFrom));
        }
        if (!Objects.isNull(deliveryAtTo)) {
            query.setParameter("deliveryAtTo", DateUtils.truncateDate(deliveryAtTo));
        }
        if (!Objects.isNull(orderNumberFrom)) {
            query.setParameter("orderNumberFrom", orderNumberFrom);
        }
        if (!Objects.isNull(orderNumberTo)) {
            query.setParameter("orderNumberTo", orderNumberTo);
        }
        if (!Objects.isNull(deliveryNumberFrom)) {
            query.setParameter("deliveryNumberFrom", org.apache.commons.lang3.StringUtils.leftPad(deliveryNumberFrom, NUMBER_DIGIT, "0"));
        }
        if (!Objects.isNull(deliveryNumberTo)) {
            query.setParameter("deliveryNumberTo", org.apache.commons.lang3.StringUtils.leftPad(deliveryNumberTo, NUMBER_DIGIT, "0"));
        }
        if (!Objects.isNull(deliveryRequestNumberFrom)) {
            query.setParameter("deliveryRequestNumberFrom", org.apache.commons.lang3.StringUtils.leftPad(deliveryRequestNumberFrom, NUMBER_DIGIT, "0"));
        }
        if (!Objects.isNull(deliveryRequestNumberTo)) {
            query.setParameter("deliveryRequestNumberTo", org.apache.commons.lang3.StringUtils.leftPad(deliveryRequestNumberTo, NUMBER_DIGIT, "0"));
        }
    }

    /**
     * クエリに品番のパラメータを設定する.
     * @param partNoPartialMatchList 品番検索条件リスト(部分一致)
     * @param query クエリ
     */
    public void setQueryParametersByPartNo(final List<String> partNoPartialMatchList, final Query query) {
        // 先頭1件はANDでつなぐため単独で取り出す
        final String firstpartNo = partNoPartialMatchList.get(0);
        query.setParameter("firstPartNo", firstpartNo);

        // 2件目以降はORでつなぐ
        // ※subListはインデックスの終わりの要素を含まない
        final List<String> afterSecondPartNoList = partNoPartialMatchList.subList(1, partNoPartialMatchList.size());
        if (!afterSecondPartNoList.isEmpty()) {
            for (int i = 0; i < afterSecondPartNoList.size(); i++) {
                query.setParameter("partNo" + i, afterSecondPartNoList.get(i));
            }
        }
    }

    /**
     * SELECT句を作成する.
     * @param sql sql
     */
    private void generateSelectPhrase(final StringBuilder sql) {
        sql.append(SELECT_PHRASE_SQL);
        // PRD_0001 del SIT start
        //sql.append(" ,");
        //sql.append(TRANSACTION_LOT_SUB_QUERY);
        //sql.append(" ,");
        //sql.append(ALLOCATION_LOT_SUB_QUERY);
        //sql.append(" AS allocation_lot ,");
        //sql.append(FIX_ARRIVAL_COUNT_SQL);
        //sql.append(" AS fix_arrival_count");
        // PRD_0001 del SIT end
    }

    /**
     * FROM句を作成する.
     * @param searchCondition 配分一覧検索条件
     * @param sql sql
     */
    private void generateFromPhrase(final DeliverySearchListConditionModel searchCondition, final StringBuilder sql) {
        // 検索条件
        final CarryType carryType = searchCondition.getCarryType();
        final Boolean orderCompleteFlg = searchCondition.getOrderCompleteFlg();
        final String mdfMakerCode = searchCondition.getMdfMakerCode();

        // FROM句作成
        sql.append(" FROM t_delivery d");
        sql.append("   INNER JOIN t_delivery_detail dd");
        sql.append("           ON d.id = dd.delivery_id");
        sql.append("          AND dd.deleted_at IS NULL");
        // PRD_0001 del SIT start
        //sql.append("   LEFT OUTER JOIN t_purchase p");
        //sql.append("           ON d.id = p.delivery_id");
        //sql.append("          AND p.delivery_id IS NULL");
        // PRD_0001 del SIT end

        if (!Objects.isNull(carryType)) {
            sql.append("      AND dd.carry_type = :carryType");
            // PRD_0104#7055 add JFE start
            // PRD_0128 #9891 mod JFE start
            //if(CarryType.TC.equals(carryType)) {
            if(!CarryType.DIRECT.equals(carryType)) {
            // PRD_0128 #9891 mod JFE end
            	sql.append("   INNER JOIN m_delivery_destination mdd");
            	sql.append("           ON dd.logistics_code = mdd.logistics_code");
            	sql.append("   INNER JOIN m_delivery_location mdl");
            	sql.append("           ON mdd.delivery_location_id = mdl.id");
            	// PRD_0128 #9891 mod JFE start
            	//sql.append("           AND mdl.tc_flg = 1");
            	sql.append("           AND mdl.tc_flg = ");
            	sql.append(CarryType.TC.equals(carryType)?"1":"0");
            	// PRD_0128 #9891 mod JFE end
            	//キャリー区分はTCでも、carry_typeの値は通常の値だ
            	searchCondition.setCarryType(CarryType.NORMAL);
            }
            // PRD_0104#7055 add JFE end
        }

        // 範囲条件追加(納品日、発注番号、納品番号、納品依頼番号)
        generateFromPhraseByRange(searchCondition, sql);

        sql.append("   INNER JOIN t_order o");
        sql.append("           ON d.order_id = o.id");
        sql.append("          AND o.deleted_at IS NULL");
        if (!StringUtils.isEmpty(mdfMakerCode)) {
            sql.append("      AND o.mdf_maker_code = :mdfMakerCode");
        }
        if (!Objects.isNull(orderCompleteFlg)) {
            if (orderCompleteFlg) {
                sql.append("  AND ( o.product_complete_order = " + CompleteOrderType.AUTO_COMPLETE.getValue()
                + " OR o.product_complete_order = " + CompleteOrderType.COMPLETE.getValue()
                + " OR o.all_completion_type = " + CompleteType.COMPLETE.getValue()
                + " )");
            } else {
                sql.append("  AND ( o.product_complete_order = " + CompleteOrderType.INCOMPLETE.getValue()
                               + " OR o.product_complete_order IS NULL ) ");
                sql.append(" AND ( o.all_completion_type = " + CompleteType.INCOMPLETE.getValue()
                               + " OR o.all_completion_type IS NULL )");
            }
        }

        sql.append("   INNER JOIN t_item i");
        sql.append("           ON d.part_no_id = i.id");
        sql.append("          AND i.deleted_at IS NULL");

        sql.append("   LEFT OUTER JOIN m_codmst m");
        sql.append("           ON i.brand_code = m.code1");
        sql.append("          AND tblid = '02'");
        sql.append("          AND m.mntflg IN ('1', '2', '')");
        sql.append("          AND m.deleted_at IS NULL");
        //PRD_0127 #9837 add JFE start
        sql.append("   INNER JOIN m_delivery_destination mdd2");
        sql.append("           ON dd.logistics_code = mdd2.logistics_code");
        sql.append("   INNER JOIN m_delivery_location mdl2");
        sql.append("           ON mdd2.delivery_location_id = mdl2.id");
        //PRD_0127 #9837 add JFE end
        // PRD_0001 add SIT start
        sql.append("   LEFT OUTER JOIN ");
        sql.append(TRANSACTION_LOT_SUB_QUERY);
        sql.append("   tl ");
        sql.append("           ON dd.id = tl.delivery_detail_id");
        //PRD_0113 #7411  JFE del start
//        sql.append("   LEFT OUTER JOIN ");
//        sql.append(ALLOCATION_LOT_SUB_QUERY);
//        sql.append("   al ");
//        sql.append("           ON d.id = al.delivery_id");
        //PRD_0113 #7411  JFE del end
        sql.append("   LEFT OUTER JOIN ");
        sql.append(FIX_ARRIVAL_COUNT_SQL);
        sql.append("   fac ");
        sql.append("           ON d.id = fac.delivery_id");
        // PRD_0001 add SIT end
    }

    /**
     * 範囲が指定されている検索条件を含むFROM句作成.
     * 範囲が指定されている項目：納品日、発注番号、納品番号、納品依頼番号
     *
     * @param searchCondition 配分一覧検索条件
     * @param sql sql
     */
    private void generateFromPhraseByRange(final DeliverySearchListConditionModel searchCondition, final StringBuilder sql) {
        final Date deliveryAtFrom = searchCondition.getDeliveryAtFrom();
        final Date deliveryAtTo = searchCondition.getDeliveryAtTo();
        final BigInteger orderNumberFrom = searchCondition.getOrderNumberFrom();
        final BigInteger orderNumberTo = searchCondition.getOrderNumberTo();
        final String deliveryNumberFrom = searchCondition.getDeliveryNumberFrom();
        final String deliveryNumberTo = searchCondition.getDeliveryNumberTo();
        final String deliveryRequestNumberFrom = searchCondition.getDeliveryRequestNumberFrom();
        final String deliveryRequestNumberTo = searchCondition.getDeliveryRequestNumberTo();

        // 納品日(修正納期)
        if (!Objects.isNull(deliveryAtFrom)) {
            sql.append(" AND :deliveryAtFrom <= dd.correction_at");
        }
        if (!Objects.isNull(deliveryAtTo)) {
            sql.append(" AND dd.correction_at <= :deliveryAtTo");
        }

        // 発注番号
        if (!Objects.isNull(orderNumberFrom)) {
            sql.append(" AND :orderNumberFrom <= d.order_number");
        }
        if (!Objects.isNull(orderNumberTo)) {
            sql.append(" AND d.order_number <= :orderNumberTo");
        }

        // 納品番号
        if (!Objects.isNull(deliveryNumberFrom)) {
            sql.append(" AND :deliveryNumberFrom <= dd.delivery_number");
        }
        if (!Objects.isNull(deliveryNumberTo)) {
            sql.append(" AND dd.delivery_number <= :deliveryNumberTo");
        }

        // 納品依頼番号
        if (!Objects.isNull(deliveryRequestNumberFrom)) {
            sql.append(" AND :deliveryRequestNumberFrom <= dd.delivery_request_number");
        }
        if (!Objects.isNull(deliveryRequestNumberTo)) {
            sql.append(" AND dd.delivery_request_number <= :deliveryRequestNumberTo");
        }
    }

    /**
     * WHERE句を作成する.
     * @param searchCondition 配分一覧検索条件
     * @param sql sql
     */
    public void generateWherePhrase(final DeliverySearchListConditionModel searchCondition, final StringBuilder sql) {
        // キーワード検索条件、分割した結果上限値を超える場合はエラーをthrowする
        final List<String> partNoList = deliverySearchListComponent.splitPartialMatchConditions(searchCondition.getPartNo()); // 品番
        final List<String> brandCodeList = deliverySearchListComponent.splitPerfectMatchConditions(searchCondition.getBrandCode()); // ブランドコード
        // PRD_0011 mod SIT start
        //final List<String> departmentCodeList = deliverySearchListComponent.splitPerfectMatchConditions(searchCondition.getDepartmentCode()); // 事業部コード
        final String departmentCode = searchCondition.getDepartmentCode(); // 事業部コード
        // PRD_0011 mod SIT end
        final Boolean purchasesFlg = searchCondition.getPurchasesFlg(); // 仕入れフラグ
        final Boolean approvaldFlg = searchCondition.getApprovaldFlg(); // 承認フラグ
        final Boolean shipmentFlg = searchCondition.getShipmentFlg(); // 出荷フラグ
        // PRD_0037 mod SIT start
        //final Boolean reAllocationFlg = searchCondition.getReAllocationFlg(); // 要再配分
        final DeliveryListAllocationStatusType allocationStatusType = searchCondition.getAllocationStatusType(); // 配分状態
        // PRD_0037 mod SIT end

        sql.append(" WHERE d.deleted_at IS NULL");
        // PRD_0011 mod SIT start
        //if (!partNoList.isEmpty() && !brandCodeList.isEmpty() && !departmentCodeList.isEmpty()) {
        if (!partNoList.isEmpty() && !brandCodeList.isEmpty()) {
        // PRD_0011 mod SIT end
            // PRD_0087 mod SIT start
            //sql.append("      AND ");
            //generateWherePhraseByPartNo(partNoList, sql); // 品番の部分一致、複数検索クエリ
            //sql.append("      OR i.brand_code IN (:brandCode)");
            //// PRD_0011 del SIT start
            //allocationStatusType//sql.append("      OR m.item4 IN (:departmentCode)");
            //// PRD_0011 del SIT end
            sql.append("      AND (");
            generateWherePhraseByPartNo(partNoList, sql); // 品番の部分一致、複数検索クエリ
            sql.append("      OR i.brand_code IN (:brandCode))");
            // PRD_0087 mod SIT end
        } else if (!partNoList.isEmpty()) {
            sql.append("      AND ");
            generateWherePhraseByPartNo(partNoList, sql); // 品番の部分一致、複数検索クエリ
        } else if (!brandCodeList.isEmpty()) {
            sql.append("      AND i.brand_code IN (:brandCode)");
        // PRD_0011 del SIT start
        //} else if (!departmentCodeList.isEmpty()) {
        //    sql.append("      AND m.item4 IN (:departmentCode)");
        // PRD_0011 del SIT end
        }
        // PRD_0011 add SIT start
        if (!StringUtils.isEmpty(departmentCode)) {
            sql.append("      AND m.item4 = :departmentCode ");
        }
        // PRD_0011 add SIT end
        if (!StringUtils.isEmpty(purchasesFlg)) {
            sql.append("      AND dd.arrival_flg = ");
            sql.append(purchasesFlg);
        }
        if (!StringUtils.isEmpty(approvaldFlg)) {
            sql.append("      AND d.delivery_approve_status = ");
            sql.append(approvaldFlg);
        }
        if (!StringUtils.isEmpty(shipmentFlg)) {
            // PRD_0087 mod SIT start
            //sql.append("      AND dd.shipping_instructions_flg = ");
            //sql.append(shipmentFlg);
            if (shipmentFlg) {
                sql.append("      AND  dd.allocation_complete_at IS NOT NULL ");
                sql.append("      AND  dd.allocation_record_at IS NOT NULL ");
            } else {
                sql.append("      AND dd.allocation_complete_at IS NULL ");
                sql.append("      AND dd.allocation_record_at IS NULL ");
            }
            // PRD_0087 mod SIT start
        }
        // PRD_0037 mod SIT start
        //if (Objects.nonNull(reAllocationFlg) && reAllocationFlg) {
        //    sql.append("      AND ");
        //    // PRD_0001 mod SIT start
        //    //sql.append(FIX_ARRIVAL_COUNT_SQL);
        //    //sql.append("          <");
        //    //sql.append(ALLOCATION_LOT_SUB_QUERY);
        //    sql.append("      COALESCE(fac.fix_arrival_count, 0)");
        //    sql.append("          <");
        //    sql.append("      COALESCE(al.allocation_lot, 0)");
        //    // PRD_0001 mod SIT end
        //}
        if (!Objects.isNull(allocationStatusType)) {
            switch (allocationStatusType) {
                // 未配分（全店配分のみ、得意先未配分）
                case UNALLOCATED:
                    sql.append("      AND dd.store_registered_flg = " + Boolean.FALSE);
                    sql.append("      AND dd.arrival_flg = " + Boolean.FALSE);
                    break;
                // 配分済（得意先配分済、仮仕入未実施）
                case ALLOCATED:
                    sql.append("      AND dd.store_registered_flg = " + Boolean.TRUE);
                    break;
                // 要再配分（得意先配分済、仮仕入時　全店配分数＜＞入荷数）
                case REALLOCATION:
                    sql.append("      AND dd.store_registered_flg = " + Boolean.TRUE);
                    sql.append("      AND dd.arrival_flg = " + Boolean.TRUE);
                    sql.append("      AND COALESCE(fac.fix_arrival_count, 0)");
                    sql.append("          <");
                    sql.append("          COALESCE(al.allocation_lot, 0)");
                    break;
                // 入荷済未配分（得意先配分未実施、仮仕入済）
                case PURCHASEUNALLOCATED:
                    sql.append("      AND dd.store_registered_flg = " + Boolean.FALSE);
                    sql.append("      AND dd.arrival_flg = " + Boolean.TRUE);
                    break;
            default:
                break;
            }
        }
        // PRD_0037 mod SIT end
    }

    /**
     * 品番の部分一致、複数検索を含むWHERE句の作成.
     * @param partNoPartialMatchList 品番検索条件リスト(部分一致)
     * @param sql sql
     */
    public void generateWherePhraseByPartNo(final List<String> partNoPartialMatchList, final StringBuilder sql) {
        // 先頭1件はANDでつなぐため単独で取り出す
        sql.append(" d.part_no LIKE :firstPartNo");

        // 2件目以降はORでつなぐ
        // ※subListはインデックスの終わりの要素を含まない
        final List<String> afterSecondPartNoList = partNoPartialMatchList.subList(1, partNoPartialMatchList.size());
        if (!afterSecondPartNoList.isEmpty()) {
            for (int i = 0; i < afterSecondPartNoList.size(); i++) {
                sql.append(" OR d.part_no LIKE :partNo" + i);
            }
        }
    }

    /**
     * TDeliverySearchResultEntityの件数を取得する.
     * @param searchCondition 配分一覧検索条件
     * @param fromPhraseSqlParam From句(任意)
     * @param wherePhraseSqlParam Where句(任意)
     * @return 件数
     */
    @Override
    public int countBySpec(final DeliverySearchListConditionModel searchCondition,
            final StringBuilder fromPhraseSqlParam, final StringBuilder wherePhraseSqlParam) {
        final StringBuilder cntSql = new StringBuilder();
        cntSql.append(SELECT_CNT_PHRASE_SQL);

        StringBuilder fromPhraseSql = fromPhraseSqlParam;
        if (fromPhraseSqlParam == null) {
            fromPhraseSql = new StringBuilder();
            generateFromPhrase(searchCondition, fromPhraseSql);
        }
        cntSql.append(fromPhraseSql);

        StringBuilder wherePhraseSql = wherePhraseSqlParam;
        if (wherePhraseSqlParam == null) {
            wherePhraseSql = new StringBuilder();
            generateWherePhrase(searchCondition, wherePhraseSql);
        }
        cntSql.append(wherePhraseSql);

        final Query cntQuery = entityManager.createNativeQuery(cntSql.toString());
        setQueryParameters(searchCondition, cntQuery);

        return ((Number) cntQuery.getSingleResult()).intValue();
    }
}
