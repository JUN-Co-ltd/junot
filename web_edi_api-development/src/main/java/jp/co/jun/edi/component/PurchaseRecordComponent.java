//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;

/**
 * 仕入実績関連のコンポーネント.
 */
@Component
public class PurchaseRecordComponent extends GenericComponent {
    @PersistenceContext
    private EntityManager entityManager;
    //PRD_0152 del JFE start
//    // デリスケCSV SELECT句
//    private static final String CSV_SELECT_PHRASE_SQL =
//            "  SELECT p.id AS id"
//            + "     , sir.name AS supplier_name"
//            + "     , p.supplier_code AS supplier_code"
//            + "     , locat.company_name AS arrival_place"
//            + "     , p.arrival_place AS logistics_code"
//            + "     , p.record_at AS record_at"
//            + "     , p.purchase_voucher_number AS purchase_voucher_number"
//            + "     , p.purchase_type AS purchase_type"
//            + "     , p.part_no AS part_no"
//            + "     , p.fix_arrival_count AS fix_arrival_count"
//            + "     , 0 AS m_kyu"
//            + "     , p.purchase_unit_price AS purchase_unit_price"
//            + "     , fix_arrival_count * purchase_unit_price AS unit_price_sum"
//            + "     , SUM(p.fix_arrival_count) AS fix_arrival_count_sum"
//            + "     , 0 AS m_kyu_sum"
//            + "     , SUM(fix_arrival_count * purchase_unit_price) AS unit_price_sum_total";
//    /** デリスケCSV GROUP BY句. */
//    private static final String CSV_GROUP_BY_PHRASE_SQL =
//            "  GROUP BY p.purchase_voucher_number";
//
//    /** デリスケCSV ORDER BY句. */
//    private static final String CSV_ORDER_BY_PHRASE_SQL =
//            "  ORDER BY p.record_at DESC";
//    /**
//     * デリスケCSV取得SQL文を作成する.
//     *
//     * @param searchCondition 仕入実績一覧検索条件
//     * @param sql sql
//     */
//
//    public void generatePurchaseRecordCsvSql(final PurchaseRecordSearchConditionModel searchCondition, final StringBuilder sql) {
//        generateDelischeCsvSelectPhrase(sql);
//        generateDelischeCsvFromPhrase(searchCondition, sql);
//        generateDelischeCsvWherePhrase(searchCondition, sql);
//        sql.append(CSV_GROUP_BY_PHRASE_SQL);
//        sql.append(CSV_ORDER_BY_PHRASE_SQL);
//    }
//
//
//    /**
//     *仕入実績CSV取得のSELECT句を作成する.
//     * @param sql sql
//     */
//    private void generateDelischeCsvSelectPhrase(final StringBuilder sql) {
//        sql.append(CSV_SELECT_PHRASE_SQL);
//    }
    //PRD_0152 del JFE end
    /**
     * 仕入実績CSV取得のFROM句を作成する.
     * @param searchCondition 仕入実績発注検索条件
     * @param sql sql
     */
    public void generateDelischeCsvFromPhrase(final PurchaseRecordSearchConditionModel searchCondition, final StringBuilder sql) {

        sql.append(" FROM t_purchase p");
		//仕入先表示用
		sql.append(" INNER JOIN m_sirmst sir");
		sql.append(" ON p.supplier_code = sir.sire");
		sql.append(" AND sir.mntflg IN ('1', '2', '')");
		sql.append(" AND sir.deleted_at IS NULL");

		//入荷場所を物流コードから納入場所マスタの名称を取得
		sql.append(" INNER JOIN m_delivery_destination dest");
		sql.append(" ON p.arrival_place = dest.logistics_code");
		sql.append(" AND dest.deleted_at IS NULL");

		sql.append(" INNER JOIN m_delivery_location locat");
		sql.append(" ON dest.delivery_location_id = locat.id");
		sql.append(" AND locat.deleted_at IS NULL");
    }

    /**
     * 仕入実績CSV取得のWHERE句を作成する.
     * @param searchCondition 仕入実績発注検索条件
     * @param sql sql
     */
    public void generateDelischeCsvWherePhrase(final PurchaseRecordSearchConditionModel searchCondition, final StringBuilder sql) {
    	//検索条件項目を取得
    			//PRD_0152 del JFE start
    			//final String arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
    			 //PRD_0152 del JFE end
    			final String divisionCode = searchCondition.getDivisionCode();//事業部
    			final Date recordFrom = searchCondition.getRecordAtFrom();//計上日from
    			final Date recordTo = searchCondition.getRecordAtTo();//計上日to
    			final List<String> SirCodes = getSplitConditions(searchCondition.getSirCodes());//仕入先リスト
    			final List<String> partNoList = getSplitConditions(searchCondition.getPartNo());//品番リスト
//    			final PurchaseRecordType purchaseType = searchCondition.getPurchaseType(); // 仕入区分
    			final Integer purchaseType = searchCondition.getPurchaseType(); // 仕入区分

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
    				case 0: //未選択
    	    			//入荷場所
    	    				sql.append("      AND p.arrival_place = :arrivalShop");
    					break;
    				case 1: //追加仕入
    					sql.append(" AND p.data_type = 'KR'");
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
    	    			//入荷場所
    	    				sql.append("      AND p.arrival_place = :arrivalShop");
    					break;
    				}
    			}
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

		return conditionsList;

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
     * クエリにパラメータを設定する.
     * @param sql SQL文
     * @param searchCondition 検索条件
     * @param query クエリ
     */
    public void setQueryParameters(final StringBuilder sql, final PurchaseRecordSearchConditionModel searchCondition,
            final Query query) {
    	final String arrivalShop = searchCondition.getArrivalShop().substring(4, 6); //入荷場所
		final String divisionCode = searchCondition.getDivisionCode();//事業部
		final Date recordFrom = searchCondition.getRecordAtFrom();//計上日from
		final Date recordTo = searchCondition.getRecordAtTo();//計上日to
		final List<String> SirCodes = getSplitConditions(searchCondition.getSirCodes());//仕入先リスト
		final List<String> partNoList = getSplitConditions(searchCondition.getPartNo());//品番リスト
//		final PurchaseRecordType purchaseType = searchCondition.getPurchaseType(); // 仕入区分
		final Integer purchaseType = searchCondition.getPurchaseType(); // 仕入区分

		if (!StringUtils.isEmpty(arrivalShop)) {
			if (purchaseType == 0) {
			query.setParameter("arrivalShop", arrivalShop);
			}
		}

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

}
//PRD_0133 #10181 add JFE end