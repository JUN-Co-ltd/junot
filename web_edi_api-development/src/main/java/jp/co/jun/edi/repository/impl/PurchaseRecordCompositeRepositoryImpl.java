//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.repository.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.PurchaseRecordCompositeEntity;
import jp.co.jun.edi.entity.PurchaseRecordSumCompositeEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.repository.custom.PurchaseRecordCompositeRepositoryCustom;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕入実績一覧情報Repositoryの実装クラス.
 */
@Slf4j
public class PurchaseRecordCompositeRepositoryImpl implements PurchaseRecordCompositeRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;

	private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".repository";

	@Value("${" + PROPERTY_NAME_PREFIX
			+ ".specification.extended-titem-list-specification.keyword-conditions-limit-size}")
	private int keywordConditionsLimitSize;

	/**
	 * 仕入実績一覧情報を取得する.
	 */
	@Override
	public Page<PurchaseRecordCompositeEntity> findBySearchCondition(
			final PurchaseRecordSearchConditionModel searchCondition,
			final Pageable pageable) {

		final StringBuilder sqlWhere = new StringBuilder();

		final Map<String, Object> parameterMap = new HashMap<>();

		generateWherePhrase(sqlWhere, parameterMap, searchCondition);

		final StringBuilder sql = new StringBuilder();
		generateSelectPhrase(sql);

		final StringBuilder sqlFrom = new StringBuilder();
		generateFromPhrase(sqlFrom, searchCondition, parameterMap, false);

		// 件数
		final List<PurchaseRecordSumCompositeEntity> rst = countAllRecord(sqlWhere, parameterMap, searchCondition);
		int cnt = rst.get(0).getCount();

		if (cnt == 0) {
			return new PageImpl<>(Collections.emptyList(), pageable, cnt);
		}

		sql.append(sqlFrom).append(sqlWhere);

		generateGroupByPhrase(sql);
		generateOrderByPhrase(sql);

		if (log.isDebugEnabled()) {
			log.debug("sql:" + sql.toString());
		}

		final Query query = entityManager.createNativeQuery(sql.toString(), PurchaseRecordCompositeEntity.class);

		// クエリにパラメータを設定
		setQueryParameters(searchCondition, query);

		// 開始位置を設定
		query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());

		// 取得件数を設定
		query.setMaxResults(pageable.getPageSize());

		@SuppressWarnings("unchecked")
		final List<PurchaseRecordCompositeEntity> result = query.getResultList();

		//合計値の設定
		final BigInteger ArrivalCountSum = rst.get(0).getFixArrivalCountSum();
		final BigDecimal mKyuSum = rst.get(0).getMKyuSum();
		final BigInteger priceTotal = rst.get(0).getUnitPriceSumTotal();
		result.get(0).setFixArrivalCountSum(ArrivalCountSum);
		result.get(0).setMKyuSum(mKyuSum);
		result.get(0).setUnitPriceSumTotal(priceTotal);

		//伝票区分の設定
		//PRD_0195 JFE mod start
//		for (int i = 0; i < result.size(); i++) {
//			String pType =  result.get(i).getPurchaseType(); //仕入区分
//			String sirkbn = result.get(i).getSirkbn();//仕入先区分
//			String hmk = result.get(i).getExpense_item();//費目
//			Integer orderNo = result.get(i).getOrder_no();//発注No
//
//			String denk1 = "";
//			String denk2 = "";
//	         //PRD_0193 #11702 JFE add start
//            String logCode = result.get(i).getLogisticsCode();//入荷場所
//            String pCount = result.get(i).getPurchase_count();//引取回数
//            if((pType.equals("3")||pType.equals("9")) && (logCode.equals("18")||logCode.equals("19"))) {
//                result.get(i).setPurchaseType(pCount);
//                continue;
//            }
//            //PRD_0193 #11702 JFE add end
//		    // PRD_0189 #10181 jfe add start
//			if(orderNo != null && pType != null && sirkbn != null && hmk != null) {
//			// PRD_0189 #10181 jfe add end
//
//			//伝区の上桁を設定
//			if (sirkbn.equals("00")) {
//				if(pType.equals("3")) {//返品(3)の場合
//					denk1 = "6";//振替仕入返品
//				}else {
//					denk1 = "5";//振替仕入
//				}
//			}else {
//				if(pType.equals("3")) {//返品(3)の場合
//					denk1 = "4";//振替仕入返品
//				}else {
//					denk1 = "3";//振替仕入
//				}
//			}
//
//			//伝区の下桁を設定
//			if(hmk.isEmpty() || orderNo ==0) {
//				hmk = "01";
//			}
//
//			if (hmk.equals("01") || hmk.equals("04")|| hmk.equals("24") || hmk.equals("05")) {
//				denk2 = hmk.substring(hmk.length() -1);
//			}else if( hmk.equals("20")|| hmk.equals("30")) {
//				denk2 = hmk.substring(0,1);
//			}
//			// PRD_0189 #10181 jfe add start
//			}
//			// PRD_0189 #10181 jfe add end
//			result.get(i).setPurchaseType(denk1 + denk2);
//		}

		for (int i = 0; i < result.size(); i++) {
			String pType =  result.get(i).getPurchaseType(); //仕入区分
			String sirkbn = result.get(i).getSirkbn();//仕入先区分
			String hmk = result.get(i).getExpense_item();//費目
			Integer orderNo = result.get(i).getOrder_no();//発注No
            String logCode = result.get(i).getLogisticsCode();//入荷場所
            String pCount = result.get(i).getPurchase_count();//引取回数
            String denk1 = "";
            String denk2 = "";

            if(pType == null) pType = "";
            if(logCode == null) logCode = "";
            if(orderNo == null || orderNo == 0) hmk = "01"; //発注情報が存在しない場合は費目を01に設定する

            //PRD_0202 JFE mod start
//            if((pType.equals("3")||pType.equals("9")) && (logCode.equals("18")||logCode.equals("19"))) {
//                result.get(i).setPurchaseType(pCount);
//                continue;
//            }
//
//            if(pType.equals("2")) {
//                result.get(i).setPurchaseType("32");
//                continue;
//            }
//            if(pType.equals("4")) {
//                result.get(i).setPurchaseType("33");
//                continue;
//            }
//            if(pType.equals("5")) {
//                result.get(i).setPurchaseType("");
//                continue;
//            }
            if(pType.equals("2")||pType.equals("5")||pType.equals("9")){
                result.get(i).setPurchaseType(pCount);
                continue;
            }

            if(pType.equals("4")) {
                result.get(i).setPurchaseType("33");
                continue;
            }
            if(pType.equals("3")&&(logCode.equals("18")||logCode.equals("19"))) {
                result.get(i).setPurchaseType(pCount);
                continue;
            }
            //PRD_0202 JFE mod end

            if(sirkbn != null) { //仕入先マスタの仕入先区分が見つかった場合
			//伝区の上桁を設定
			if (sirkbn.equals("00")) {
				if(pType.equals("3")) {//返品(3)の場合
					denk1 = "6";//振替仕入返品
				}else {
					denk1 = "5";//振替仕入
				}
			}else {
				if(pType.equals("3")) {//返品(3)の場合
                        denk1 = "4";//仕入返品
				}else {
                        denk1 = "3";//仕入
				}
			}

			//伝区の下桁を設定
			if (hmk.equals("01") || hmk.equals("04")|| hmk.equals("24") || hmk.equals("05")) {
				denk2 = hmk.substring(hmk.length() -1);
			}else if( hmk.equals("20")|| hmk.equals("30")) {
				denk2 = hmk.substring(0,1);
			}
			// PRD_0189 #10181 jfe add start
			}
			// PRD_0189 #10181 jfe add end
			result.get(i).setPurchaseType(denk1 + denk2);
		}
		//PRD_0195 JFE mod end

		return new PageImpl<>(result, pageable, cnt);
	}

	/**
	 * クエリにパラメータを設定する.
	 * @param searchCondition 配分一覧検索条件
	 * @param query クエリ
	 */
	private void setQueryParameters(final PurchaseRecordSearchConditionModel searchCondition, final Query query) {
	    //PRD_0174 #10181　JFE mod start
//		final String arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
	    String arrivalShop = "";
	    if(searchCondition.getArrivalShop() == null || searchCondition.getArrivalShop().equals("")) {
	    }else{
	        //PRD_0190 mod JFE start
//	        arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
	        String arrivalHeader = searchCondition.getArrivalShop().substring(0, 4);
	        if(arrivalHeader.equals("5551")) {
	            arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所 SQ
	        }else {
	            arrivalShop = searchCondition.getArrivalShop().substring(4, 5); //入荷場所 SQ以外
	        }
            //PRD_0190 mod JFE end
	    }
		//PRD_0174 #10181　JFE mod end
		final String divisionCode = searchCondition.getDivisionCode();//事業部
		final Date recordFrom = searchCondition.getRecordAtFrom();//計上日from
		final Date recordTo = searchCondition.getRecordAtTo();//計上日to
		final List<String> SirCodes = getSplitConditions(searchCondition.getSirCodes());//仕入先リスト
		final List<String> partNoList = getSplitConditions(searchCondition.getPartNo());//品番リスト
		final List<String> comCodeList = getSplitConditions(searchCondition.getComCode());//会社コードリスト
		//PRD_0190　JFE del start
		//final Integer purchaseType = searchCondition.getPurchaseType(); // 仕入区分
		//PRD_0190　JFE del end

		//PRD_0174 #10181　JFE mod start
//		if (!StringUtils.isEmpty(arrivalShop)) {
		if (!arrivalShop.equals("")) {
		    //PRD_0190 mod JFE start
//		    if (purchaseType == 0 || purchaseType == 1) {
//			query.setParameter("arrivalShop", arrivalShop);
//			}
			query.setParameter("arrivalShop", arrivalShop);
            //PRD_0190 mod JFE end
		}
		//PRD_0174 #10181　JFE mod end

		if (divisionCode != null && divisionCode.length() != 0) {
			query.setParameter("divisionCode", divisionCode);
		}

		if (!Objects.isNull(recordFrom)) {
			query.setParameter("recordAtFrom", recordFrom);
		}
		if (!Objects.isNull(recordTo)) {
			query.setParameter("recordAtTo", recordTo);
		}
		if (!partNoList.isEmpty()) {
			setQueryParametersByPartNo(partNoList, query);
		}
		if (!SirCodes.isEmpty()) {
			setQueryParametersByMakerCode(SirCodes, query);
		}
		if (!comCodeList.isEmpty()) {
			query.setParameter("comCodeList", comCodeList);
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
		query.setParameter("firstPartNo", firstpartNo + "%");

		// 2件目以降はORでつなぐ
		// ※subListはインデックスの終わりの要素を含まない
		final List<String> afterSecondPartNoList = partNoPartialMatchList.subList(1, partNoPartialMatchList.size());
		if (!afterSecondPartNoList.isEmpty()) {
			for (int i = 0; i < afterSecondPartNoList.size(); i++) {
				query.setParameter("partNo" + i, afterSecondPartNoList.get(i) + "%");
			}
		}
	}

	/**
	 * クエリに仕入コードのパラメータを設定する.
	 * @param partNoPartialMatchList 仕入先検索条件リスト
	 * @param query クエリ
	 */
	public void setQueryParametersByMakerCode(final List<String> MakerCodePartialMatchList, final Query query) {
		// 先頭1件はANDでつなぐため単独で取り出す
		final String firstMakerCode = MakerCodePartialMatchList.get(0);
		query.setParameter("firstMakerCode", firstMakerCode);

		// 2件目以降はORでつなぐ
		// ※subListはインデックスの終わりの要素を含まない
		final List<String> afterSecondPartNoList = MakerCodePartialMatchList.subList(1,
				MakerCodePartialMatchList.size());
		if (!afterSecondPartNoList.isEmpty()) {
			for (int i = 0; i < afterSecondPartNoList.size(); i++) {
				query.setParameter("MakerCode" + i, afterSecondPartNoList.get(i));
			}
		}
	}

	/**
	 * @param sqlWhere WHERE句
	 * @param parameterMap パラメータ
	 * @param searchCondition 検索条件
	 * @return レコード件数
	 */
	private List<PurchaseRecordSumCompositeEntity> countAllRecord(final StringBuilder sqlWhere, final Map<String, Object> parameterMap,
			final PurchaseRecordSearchConditionModel searchCondition) {
		final StringBuilder sqlCount = new StringBuilder();
		final StringBuilder sqlFrom = new StringBuilder();

		sqlCount.append(" SELECT COUNT(rslt.cnt) AS count,");
		sqlCount.append(" SUM(fac) AS fix_arrival_count_sum,");
		sqlCount.append(" SUM(msu) AS m_kyu_sum,");
		sqlCount.append(" SUM(pup) AS unit_price_sum_total");
		sqlCount.append(" FROM (SELECT COUNT(p.id) AS cnt, ");
		//PRD_0150 #10181 JFE mod start
//		sqlCount.append(" CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN p.purchase_type NOT IN (2 , 4, 5) THEN p.fix_arrival_count ELSE 0 END AS fac,");
//		sqlCount.append(" TRUNCATE(CASE WHEN p.purchase_type IN (2 , 4, 5) THEN (p.fix_arrival_count / 100) ELSE 0 END,2) AS msu, ");
//		//m級は後で入れる
//		sqlCount.append(" CASE  WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN purchase_type IN (2 , 4, 5) THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END AS pup");
		//PRD_0182 #10181 JFE mod start
//		sqlCount.append(" CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END AS fac,");
//		sqlCount.append(" TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2) AS msu, ");
//		sqlCount.append(" CASE  WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END AS pup");
		//PRD_0193 #11702 JFE mod start
//		sqlCount.append(" SUM(CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END) AS fac,");
//        sqlCount.append(" SUM(TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2)) AS msu, ");
//        sqlCount.append(" SUM(CASE  WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END )AS pup");
        sqlCount.append(" SUM(CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END) AS fac,");
        sqlCount.append(" SUM(TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2)) AS msu, ");
        sqlCount.append(" SUM(CASE  WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END )AS pup");
        //PRD_0193 #11702 JFE mod end
		//PRD_0150 #10181 JFE mod end
		//PRD_0182 #10181 JFE mod end
		generateFromPhrase(sqlFrom, searchCondition, parameterMap, true);
		sqlCount.append(sqlFrom).append(sqlWhere);

		generateGroupByPhrase(sqlCount);

		sqlCount.append(" ) AS rslt ");

		if (log.isDebugEnabled()) {
			log.debug("sqlCount:" + sqlCount.toString());
		}

		final Query query = entityManager.createNativeQuery(sqlCount.toString(), PurchaseRecordSumCompositeEntity.class);
		setQueryParameters(searchCondition, query);
		return QueryUtils.list(query, parameterMap);
	}


	/**
	 * SELECT句を生成.
	 * @param sql sql
	 */
	private void generateSelectPhrase(final StringBuilder sql) {
		final List<String> sqlColumns = new ArrayList<>();
		sqlColumns.add("p.id AS id");
		sqlColumns.add("sir.name AS supplier_name");
		sqlColumns.add("p.supplier_code AS supplier_code");
		sqlColumns.add("locat.company_name AS arrival_place");
		sqlColumns.add("p.arrival_place AS logistics_code");
		sqlColumns.add("p.record_at AS record_at");
		// PRD_0162 #10181 jfe mod start
//		sqlColumns.add("p.purchase_voucher_number AS purchase_voucher_number");
		// PRD_0196 jfe mod start
		//sqlColumns.add("(CASE WHEN f.id IS NULL THEN NULL ELSE p.purchase_voucher_number END) AS purchase_voucher_number");
		sqlColumns.add("p.purchase_voucher_number AS purchase_voucher_number");
		// PRD_0196 jfe mod end
		// PRD_0162 #10181 jfe mod start
		sqlColumns.add("o.expense_item AS expense_item");
		sqlColumns.add("p.purchase_type AS purchase_type");
		sqlColumns.add("sir.sirkbn AS sirkbn");
		sqlColumns.add("o.order_number AS order_no");
		sqlColumns.add("p.part_no AS part_no");
//		sqlColumns.add("p.fix_arrival_count AS fix_arrival_count");
		//PRD_0150 #10181 JFE mod start
//		sqlColumns.add("CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN p.purchase_type NOT IN (2 , 4, 5) THEN p.fix_arrival_count ELSE 0 END AS fix_arrival_count");
//		sqlColumns.add(" TRUNCATE(CASE WHEN p.purchase_type IN (2 , 4, 5) THEN (p.fix_arrival_count / 100) ELSE 0 END,2)  AS mkyu");
	    //PRD_0182 #10181 JFE mod start
//		sqlColumns.add("CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END AS fix_arrival_count");
//		sqlColumns.add(" TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2)  AS mkyu");
	    //PRD_0193 #11702 JFE mod start
//	    sqlColumns.add("SUM(CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END) AS fix_arrival_count");
//	    sqlColumns.add("SUM( TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2))  AS mkyu");
        sqlColumns.add("SUM(CASE  WHEN p.purchase_type = 3 THEN (p.fix_arrival_count * -1) WHEN NOT(purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN p.fix_arrival_count ELSE 0 END) AS fix_arrival_count");
        sqlColumns.add("SUM( TRUNCATE(CASE WHEN (purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN (p.fix_arrival_count / 100) ELSE 0 END,2))  AS mkyu");
		//PRD_0150 #10181 JFE mod end
		//PRD_0182 #10181 JFE mod end
	    //PRD_0193 #11702 JFE mod end
		sqlColumns.add("p.purchase_unit_price AS purchase_unit_price");
		//PRD_0150 #10181 JFE mod start
	    //PRD_0182 #10181 JFE mod start
//		sqlColumns.add("CASE WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5) THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END AS unit_price_sum");
//		sqlColumns.add("CASE WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END AS unit_price_sum");
		//PRD_0193 #11702 JFE mod start
//		sqlColumns.add("SUM(CASE WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND p.purchase_count = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END) AS unit_price_sum");
        sqlColumns.add("SUM(CASE WHEN purchase_type = 3 THEN (fix_arrival_count * purchase_unit_price * -1) WHEN (purchase_type IN (2 , 4, 5)  AND RIGHT(p.purchase_count,1) = 2 AND p.division_code = '20') THEN FLOOR(((fix_arrival_count / 100) * purchase_unit_price * 1)) ELSE (fix_arrival_count * purchase_unit_price) END) AS unit_price_sum");
        sqlColumns.add("p.purchase_count AS purchase_count");
		//PRD_0150 #10181 JFE mod end
	    //PRD_0182 #10181 JFE mod end
		//PRD_0193 #11702 JFE mod end
		sqlColumns.add(" 0 AS fix_arrival_count_sum");
		sqlColumns.add(" 0 AS m_kyu_sum");
		sqlColumns.add(" 0 AS unit_price_sum_total");
	    // PRD_0162 #10181 jfe add start

		// 2023/05/26 H.Saito update start
//		sqlColumns.add(" f.id AS file_info_id");
		// 2023/05/26 H.Saito update end

		// 2023/05/26 H.Saito add start
		sqlColumns.add(" COALESCE(f.id, f2.id) AS file_info_id");
		// 2023/05/26 H.Saito add end

		// PRD_0162 #10181 jfe add end
		sql.append("SELECT ").append(StringUtils.join(sqlColumns, ", "));
	}

	/**
	 * FROM句を生成.
	 * @param sql sql
	 * @param searchCondition 検索条件
	 * @param parameterMap パラメーターマップ
	 * @param isCount 件数取得用
	 */
	private void generateFromPhrase(final StringBuilder sql,
			final PurchaseRecordSearchConditionModel searchCondition,
			final Map<String, Object> parameterMap,
			final boolean isCount) {


		sql.append(" FROM t_purchase p");
		// PRD_0189 #10181 jfe mod start
//		//仕入先表示用
//		sql.append(" INNER JOIN m_sirmst sir");
//		sql.append(" ON p.supplier_code = sir.sire");
//		sql.append(" AND sir.mntflg IN ('1', '2', '')");
//		sql.append(" AND sir.deleted_at IS NULL");
//
//		//入荷場所を物流コードから納入場所マスタの名称を取得
//		sql.append(" INNER JOIN m_delivery_destination dest");
//		sql.append(" ON p.arrival_place = dest.logistics_code");
//		sql.append(" AND dest.deleted_at IS NULL");
//
//		sql.append(" INNER JOIN m_delivery_location locat");
//		sql.append(" ON dest.delivery_location_id = locat.id");
//		sql.append(" AND locat.deleted_at IS NULL");
//
//		sql.append(" INNER JOIN t_order o");
//		sql.append(" ON o.id = p.order_id");
//		sql.append(" AND o.deleted_at IS NULL");

		//PRD_0190 mod JFE start
//        sql.append(" LEFT OUTER JOIN t_order o");
//        sql.append(" ON o.id = p.order_id");
//        sql.append(" AND o.deleted_at IS NULL");
//		// PRD_0189 #10181 jfe mod end
//	      //PRD_0176 JFE add start
//        //検索条件「費目」判断用
//		//PRD_0189 #10181　JFE mod start
////        final boolean expenseProduct = searchCondition.isExpenseProcessing();//製品
//        final boolean expenseProduct = searchCondition.isExpenseProduct();//製品
//        //PRD_0189 #10181　JFE mod end
//        final boolean expenseMaterial = searchCondition.isExpenseMaterial();//生地
//        final boolean expenseAttached = searchCondition.isExpenseAttached();//附属
//        final boolean expenseProcessing = searchCondition.isExpenseProcessing();//加工
//        final boolean expenseOther = searchCondition.isExpenseOther();//その他
//        //費目のチェックボックス、どれか１つでもチェックが付いていたらSQL追加
//        if (expenseProduct || expenseMaterial || expenseAttached || expenseProcessing || expenseOther) {
//            Integer count = 0;
//            sql.append("      AND (");
//            //製品
//            if (expenseProduct) {
//                if (count == 0) {
//                    sql.append(" o.expense_item IN ('01','24','04')");
//                    count += 1;
//                } else {
//                    sql.append("OR o.expense_item IN ('01','24','04')");
//                }
//            }
//            //生地
//            if (expenseMaterial) {
//                if (count == 0) {
//                    sql.append(" o.expense_item IN ('20','24')");
//                    count += 1;
//                } else {
//                    sql.append("OR  o.expense_item IN ('20','24')");
//                }
//            }
//            //附属
//            if (expenseAttached) {
//                if (count == 0) {
//                    sql.append(" o.expense_item IN ('30')");
//                    count += 1;
//                } else {
//                    sql.append("OR  o.expense_item IN ('30')");
//                }
//            }
//            //加工
//            if (expenseProcessing) {
//                if (count == 0) {
//                    sql.append(" o.expense_item IN ('05')");
//                    count += 1;
//                } else {
//                    sql.append("OR  o.expense_item IN ('05')");
//                }
//            }
//            //その他
//            if(expenseOther) {
//                if(count == 0) {
//                    sql.append(" o.expense_item NOT IN ('01','04','05','20','24','30')");
//
//                }else {
//                    sql.append("OR  o.expense_item NOT IN ('01','04','05','20','24','30')");
//                }
//            }
//            sql.append(" )");
//        }
        final boolean expenseProduct = searchCondition.isExpenseProduct();//製品
		final boolean expenseMaterial = searchCondition.isExpenseMaterial();//生地
		final boolean expenseAttached = searchCondition.isExpenseAttached();//附属
		final boolean expenseProcessing = searchCondition.isExpenseProcessing();//加工
		final boolean expenseOther = searchCondition.isExpenseOther();//その他
		//費目のチェックボックス、どれか１つでもチェックが付いていたらSQL追加
		if (expenseProduct || expenseMaterial || expenseAttached || expenseProcessing || expenseOther) {
            //費目の絞り込みは内部結合にする。
            sql.append(" INNER JOIN t_order o");
            sql.append(" ON o.id = p.order_id");
			Integer count = 0;
			sql.append("      AND (");
			//製品
			if(expenseProduct) {
				if(count == 0) {
					sql.append(" o.expense_item IN ('01','24','04')");
					count += 1;
				}else {
					sql.append("OR o.expense_item IN ('01','24','04')");
				}
			}
			//生地
			if(expenseMaterial) {
				if(count == 0) {
					sql.append(" o.expense_item IN ('20','24')");
					count += 1;
				}else {
					sql.append("OR  o.expense_item IN ('20','24')");
				}
			}
			//附属
			if(expenseAttached) {
				if(count == 0) {
					sql.append(" o.expense_item IN ('30')");
					count += 1;
				}else {
					sql.append("OR  o.expense_item IN ('30')");
				}
			}
			//加工
			if(expenseProcessing) {
				if(count == 0) {
					sql.append(" o.expense_item IN ('05')");
                    count += 1;
				}else {
					sql.append("OR  o.expense_item IN ('05')");
				}
			}
            //その他
            if(expenseOther) {
                if(count == 0) {
                    sql.append(" o.expense_item NOT IN ('01','04','05','20','24','30')");

                }else {
                    sql.append("OR  o.expense_item NOT IN ('01','04','05','20','24','30')");
                }
            }
			sql.append(" )");
        }else {
            sql.append(" LEFT OUTER JOIN t_order o");
            sql.append(" ON o.id = p.order_id");
		}
        sql.append(" AND o.deleted_at IS NULL");
        //PRD_0190 mod JFE end

	    // PRD_0162 #10181 jfe add start
	    sql.append(" LEFT OUTER JOIN t_purchase_file_info fi");
	    sql.append(" ON fi.purchase_voucher_number = p.purchase_voucher_number");
	    sql.append(" AND fi.deleted_at IS NULL");

		sql.append(" LEFT OUTER JOIN t_file f");
	    sql.append(" ON f.id = fi.file_no_id");
	    sql.append(" AND f.deleted_at IS NULL");
	    // PRD_0162 #10181 jfe add end

		// 2023/05/26 H.Saito Add start
	    sql.append(" LEFT OUTER JOIN t_maker_return_file_info rfi");
	    sql.append(" ON rfi.voucher_number = p.purchase_voucher_number");
	    sql.append(" AND rfi.deleted_at IS NULL");

		sql.append(" LEFT OUTER JOIN t_file f2");
	    sql.append(" ON f2.id = rfi.file_no_id");
	    sql.append(" AND f2.deleted_at IS NULL");
		// 2023/05/26 H.Saito Add end

		// PRD_0189 #10181 jfe add start
        //仕入先表示用
       sql.append(" LEFT OUTER JOIN m_sirmst sir");
       sql.append(" ON p.supplier_code = sir.sire");
       sql.append(" AND sir.mntflg IN ('1', '2', '')");
       sql.append(" AND sir.deleted_at IS NULL");

       //入荷場所を物流コードから納入場所マスタの名称を取得
       //PRD_0190 mod JFE start
//       sql.append(" INNER JOIN m_delivery_destination dest");
       sql.append(" LEFT OUTER JOIN m_delivery_destination dest");
       //PRD_0190 mod JFE end
       sql.append(" ON p.arrival_place = dest.logistics_code");
       sql.append(" AND dest.deleted_at IS NULL");
       //PRD_0190 mod JFE start
//       sql.append(" INNER JOIN m_delivery_location locat");
       sql.append(" LEFT OUTER JOIN m_delivery_location locat");
       //PRD_0190 mod JFE end
       sql.append(" ON dest.delivery_location_id = locat.id");
       sql.append(" AND locat.deleted_at IS NULL");
	    // PRD_0189 #10181 jfe add end

	      //PRD_0176 JFE del start 絞り込む場所をwhere句から結合時に変更
//      //検索条件「費目」判断用
//      final boolean expenseProduct = searchCondition.isExpenseProcessing();//製品
//      final boolean expenseMaterial = searchCondition.isExpenseMaterial();//生地
//      final boolean expenseAttached = searchCondition.isExpenseAttached();//附属
//      final boolean expenseProcessing = searchCondition.isExpenseProcessing();//加工
//      final boolean expenseOther = searchCondition.isExpenseOther();//その他
//      //費目のチェックボックス、どれか１つでもチェックが付いていたらSQL追加
//      if (expenseProduct || expenseMaterial || expenseAttached || expenseProcessing || expenseOther) {
//          Integer count = 0;
//          sql.append("      AND (");
//          //製品
//          if (expenseProduct) {
//              if (count == 0) {
//                  sql.append(" o.expense_item IN ('01','24','04')");
//                  count += 1;
//              } else {
//                  sql.append("OR o.expense_item IN ('01','24','04')");
//              }
//          }
//          //生地
//          if (expenseMaterial) {
//              if (count == 0) {
//                  sql.append(" o.expense_item IN ('20','24')");
//                  count += 1;
//              } else {
//                  sql.append("OR  o.expense_item IN ('20','24')");
//              }
//          }
//          //附属
//          if (expenseAttached) {
//              if (count == 0) {
//                  sql.append(" o.expense_item IN ('30')");
//                  count += 1;
//              } else {
//                  sql.append("OR  o.expense_item IN ('30')");
//              }
//          }
//          //加工
//          if (expenseProcessing) {
//              if (count == 0) {
//                  sql.append(" o.expense_item IN ('05')");
//                    //PRD_0152 del JFE start
//                    count += 1;
//                    //PRD_0152 del JFE end
//              } else {
//                  sql.append("OR  o.expense_item IN ('05')");
//              }
//          }
//          sql.append(" )");
//      }
    //PRD_0176 JFE del end

	}

	/**
	 * WHERE句を生成.
	 * @param sql sql
	 * @param parameterMap パラメーターマップ
	 * @param searchCondition 検索条件
	 */
	private void generateWherePhrase(final StringBuilder sql, final Map<String, Object> parameterMap,
			final PurchaseRecordSearchConditionModel searchCondition) {
		//検索条件項目を取得
		final String divisionCode = searchCondition.getDivisionCode();//事業部
		final Date recordFrom = searchCondition.getRecordAtFrom();//計上日from
		final Date recordTo = searchCondition.getRecordAtTo();//計上日to
		final List<String> SirCodes = getSplitConditions(searchCondition.getSirCodes());//仕入先リスト
		final List<String> partNoList = getSplitConditions(searchCondition.getPartNo());//品番リスト
//		final PurchaseRecordType purchaseType = searchCondition.getPurchaseType(); // 仕入区分
		final Integer purchaseType = searchCondition.getPurchaseType(); // 仕入区分
		final List<String> comCodeList = getSplitConditions(searchCondition.getComCode());//会社コードリスト
		//PRD_0174 #10181　JFE add start
//      final String arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
        String arrivalShop = "";
        if(searchCondition.getArrivalShop() == null || searchCondition.getArrivalShop().equals("")) {
        }else{
            //PRD_0190 mod JFE start
//          arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
            String arrivalHeader = searchCondition.getArrivalShop().substring(0, 4);
            if(arrivalHeader.equals("5551")) {
                arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所 SQ
            }else {
                arrivalShop = searchCondition.getArrivalShop().substring(4, 5); //入荷場所 SQ以外
            }
            //PRD_0190 mod JFE end
        }
        //PRD_0174 #10181　JFE add end

		//条件を追加
		sql.append(" WHERE p.deleted_at IS NULL");
		//仕入先
		if (!SirCodes.isEmpty()) {
			sql.append("      AND (");
			generateWherePhraseBySirCodes(SirCodes, sql); // 仕入先コード、複数検索クエリ
		}
		//品番
		if (!partNoList.isEmpty()) {
			sql.append("      AND (");
			generateWherePhraseByPartNo(partNoList, sql); // 品番の部分一致、複数検索クエリ
		}
		//事業部
		if (divisionCode != null && divisionCode.length() != 0) {
			sql.append("      AND p.division_code = :divisionCode");
		}
		//計上日From
		if (null != recordFrom) {
			sql.append(" AND :recordAtFrom <= p.record_at");
		}
		//計上日To
		if (null != recordTo) {
			sql.append(" AND p.record_at <= :recordAtTo");
		}
		//仕入区分
		if (!Objects.isNull(purchaseType)) {
			switch (purchaseType) {
			//PRD_0152 del JFE start
//			case 0: //未選択
//				//入荷場所
//					sql.append("      AND p.arrival_place = :arrivalShop");
//				break;
			//PRD_0152 del JFE end
			case 1: //追加仕入
				sql.append(" AND p.data_type = 'KR'");
				//PRD_0174 #10181　JFE del start
//				sql.append(" AND p.arrival_place = :arrivalShop");
				//PRD_0174 #10181　JFE del end
				sql.append(" AND (p.purchase_type != '3' OR p.purchase_type IS NOT NULL)");
				break;
			case 2: //仕入返品
				sql.append(" AND p.data_type = 'KR'");
				sql.append(" AND p.purchase_type = '3'");
				sql.append(" AND p.arrival_place IN ('18','19')");
				break;
			case 3: //店舗発注店舗
				sql.append(" AND p.data_type = 'KR'");
				sql.append(" AND p.purchase_type = '9'");
				sql.append(" AND p.arrival_place = '18'");
				break;
			case 4: //消化委託店舗
				sql.append(" AND p.data_type = 'KR'");
				sql.append(" AND p.purchase_type = '9'");
				sql.append(" AND p.arrival_place = '19'");
				break;
			default:
			    //PRD_0174 #10181　JFE del start
				//入荷場所
//					sql.append("      AND p.arrival_place = :arrivalShop");
				//PRD_0174 #10181　JFE del end
				break;
			}
		}
		//PRD_0174 #10181　JFE add start
	    //入荷場所
        if (!arrivalShop.equals("")) {
            sql.append("      AND p.arrival_place = :arrivalShop");
        }
		//PRD_0174 #10181　JFE add end
		//会社コード
		if (!comCodeList.isEmpty()) {
			sql.append("      AND substring(p.part_no,1,2) IN (select code1 from m_codmst where tblid = '02' and  item3 IN (:comCodeList))");
		}

	}

	/**
	 * 品番の部分一致、複数検索を含むWHERE句の作成.
	 * @param partNoPartialMatchList 品番検索条件リスト(部分一致)
	 * @param sql sql
	 */
	public void generateWherePhraseByPartNo(final List<String> partNoPartialMatchList, final StringBuilder sql) {
		// 先頭1件はANDでつなぐため単独で取り出す
		sql.append(" p.part_no LIKE :firstPartNo");

		// 2件目以降はORでつなぐ
		// ※subListはインデックスの終わりの要素を含まない
		final List<String> afterSecondPartNoList = partNoPartialMatchList.subList(1, partNoPartialMatchList.size());
		if (!afterSecondPartNoList.isEmpty()) {
			for (int i = 0; i < afterSecondPartNoList.size(); i++) {
				sql.append(" OR p.part_no LIKE :partNo" + i);
			}
		}
		sql.append(" )");
	}

	/**
	 * 仕入先コードの一致、複数検索を含むWHERE句の作成.
	 * @param partNoPartialMatchList 仕入先検索条件リスト(部分一致)
	 * @param sql sql
	 */
	public void generateWherePhraseBySirCodes(final List<String> SirCodesPartialMatchList, final StringBuilder sql) {
		// 先頭1件はANDでつなぐため単独で取り出す
		sql.append(" p.supplier_code LIKE :firstMakerCode");

		// 2件目以降はORでつなぐ
		// ※subListはインデックスの終わりの要素を含まない
		final List<String> afterSecondMakerCodeList = SirCodesPartialMatchList.subList(1,
				SirCodesPartialMatchList.size());
		if (!afterSecondMakerCodeList.isEmpty()) {
			for (int i = 0; i < afterSecondMakerCodeList.size(); i++) {
				sql.append(" OR p.supplier_code LIKE :MakerCode" + i);
			}
		}
		sql.append(" )");
	}

	/**
	 * 条件分割処理.
	 * 入力値は、全半角スペースで分割する.
	 * 分割した結果上限値を超える場合はエラーをthrowする.
	 * @param conditions 分割前のテキスト
	 * @return スペースで分割した検索条件
	 *
	 */
	private List<String> getSplitConditions(final String conditions) {

		final List<String> conditionsList = jp.co.jun.edi.util.StringUtils.splitWhitespace(conditions);

		if (isConditionsOverLimit(conditionsList)) {
			// 項目上限値エラー
			throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_013));
		}

		return conditionsList;

	}

	/**
	 * 検索パラーメータが上限値以上設定されていないかチェックする.
	 * @param conditions 条件のリスト
	 * @return true：上限値を超えている
	 *          falase：条件を超えていない
	 */
	private boolean isConditionsOverLimit(final List<String> conditions) {

		if (conditions != null && conditions.size() > keywordConditionsLimitSize) {
			return true;
		}
		return false;
	}

	/**
	 * GROUP BY句を生成.
	 * @param sql sql
	 */
	private void generateGroupByPhrase(final StringBuilder sql) {
		        sql.append(" GROUP BY ");
		       sql.append(" p.purchase_voucher_number"); //伝票No
		//        sql.append(" , dd.division_code");
	}

	/**
	 * ORDER BY句を生成.
	 * @param sql sql
	 */
	private void generateOrderByPhrase(final StringBuilder sql) {
		sql.append(" ORDER BY ");
		// PRD_0189 #10181 jfe mod start
//		sql.append(" p.supplier_code ASC");
	    sql.append(" p.supplier_code IS NULL ASC");
	    // PRD_0189 #10181 jfe mod end
		sql.append(" ,p.record_at ASC");
	}
}
//PRD_0133 #10181 add JFE end
