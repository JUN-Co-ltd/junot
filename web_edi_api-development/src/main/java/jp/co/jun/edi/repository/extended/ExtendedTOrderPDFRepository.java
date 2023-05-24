package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTOrderPDFEntity;

/**
 *
 * ExtendedTOrderPDFRepository.
 *
 */
@Repository
public interface ExtendedTOrderPDFRepository extends JpaRepository<ExtendedTOrderPDFEntity, BigInteger> {

    /**
     * 受注確定PDF作成用SQL.
     * <pre>
     * SELECT
     *      t1.id as id -- 発注ID
     *    , t1.part_no_id as part_no_id -- 品番ID
     *    , t3.part_no as part_no -- 品番（製品番号）
     *    , t3.brand_code as brand_code -- ブランドコード
     *    , t3.item_code as item_code -- アイテムコード
     *    , t2.yubin as yubin -- 郵便番号
     *    , t2.add1 as address1 -- 住所1
     *    , t2.add2 as addreess2 -- 住所2
     *    , t2.add3 as addreess3 -- 住所3
     *    , t2.name as send_to_name -- 送付先名
     *    , t1.mdf_maker_code as sire -- 仕入先コード
     *    , t2.tel1 as hphone -- 送付先電話番号
     *    , t1.order_number as order_number -- 発注No
     *    , t1.expense_item as expense_item_code -- 費目 費目コード
     *    , (SELECT sq1.item1 FROM m_codmst sq1  -- 費目 費目名称
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid = '41'
     *         AND sq1.code1 = t1.expense_item) as expense_item_name
     *    , t1.product_order_at as product_order_date -- 製品発注日
     *    , t3.dept_code as division_code -- 部門コード
     *    , (SELECT sq2.item2 FROM m_codmst sq1 -- 事業部名
     *         LEFT JOIN m_codmst sq2
     *             ON sq2.deleted_at IS NULL
     *             AND sq2.mntflg IN ('1', '2', '')
     *             AND sq2.tblid = 'JG'
     *             AND sq2.code1 = sq1.item4
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='02'
     *         AND sq1.code1 = t3.brand_code) as division_name
     *    , (SELECT sq1.item1 FROM m_codmst sq1  -- アイテム名
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='03'
     *         AND sq1.code1 = t3.brand_code
     *         AND sq1.code2 = t3.item_code) as item_name
     *    , (SELECT sq1.item2 FROM m_codmst sq1  -- 原産国
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='05'
     *         AND sq1.code1 = t3.coo_code) as country_of_origin
     *    , t1.quantity as quantity -- 数量
     *    , t1.unit_price as unit_price -- 単価
     *    , t1.product_delivery_at as product_delivery_date -- 製品納期
     *    , t3.year as year -- 年度
     *    , t3.season_code as season -- シーズン
     *    , t1.retail_price as retail_price -- 上代
     *    , t3.product_name as product_name -- 品名
     *    , (SELECT sq1.item2 FROM m_codmst sq1  -- 製造担当
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='22'
     *         AND sq1.code1 = t3.mdf_staff_code) as mdf_staff
     *    , (SELECT sq1.item2 FROM m_codmst sq1  -- 企画担当
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='22'
     *         AND sq1.code1 = t3.planner_code) as planning_staff
     *    , (SELECT sq1.item2 FROM m_codmst sq1  -- パタンナー
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='22'
     *         AND sq1.code1 = t3.pataner_code) as pataner
     *    , t1.relation_number as relation_number -- 関連No
     *    , t1.attached_cost as attached_cost -- 附属代
     *    , t1.other_cost as other_cost -- その他原価
     *    , t1.product_cost as product_cost -- 製品原価
     *    , t1.application as application -- 適用
     *    , (SELECT sq2.item2 FROM m_codmst sq1 -- 会社名
     *         LEFT JOIN m_codmst sq2
     *           ON sq2.deleted_at IS NULL
     *           AND sq2.mntflg IN ('1', '2', '')
     *           AND sq2.tblid='61'
     *           AND sq2.code1 = sq1.item3
     *         WHERE sq1.deleted_at IS NULL
     *         AND sq1.mntflg IN ('1', '2', '')
     *         AND sq1.tblid='02'
     *         AND sq1.code1 = t3.brand_code ) as company_name
     *    FROM t_order t1
     *      LEFT JOIN m_sirmst t2 ON t2.sire = t1.mdf_maker_code
     *      LEFT JOIN t_item t3 ON t3.id = t1.part_no_id
     *    WHERE
     *      t1.id = 1 -- :orderId ★発注ID
     *    ORDER BY
     *      t1.id
     *   </pre>
     * @param orderId 発注ID
     * @return 拡張受注確定PDF情報を取得する
     */
    @Query(value = " SELECT"
            + "   t1.id as id" // 発注ID
            + " , t1.part_no_id as part_no_id" // 品番ID
            + " , t3.part_no as part_no" // 品番（製品番号）
            + " , t3.brand_code as brand_code" // ブランドコード
            + " , t3.item_code as item_code" // アイテムコード
            + " , t2.yubin as yubin" // 郵便番号
            + " , t2.add1 as address1" // 住所1
            + " , t2.add2 as addreess2" // 住所2
            + " , t2.add3 as addreess3" // 住所3
            + " , t2.name as send_to_name" // 送付先名
            + " , t1.mdf_maker_code as sire" // 仕入先コード
            + " , t2.tel1 as hphone" // 送付先電話番号
            + " , t1.order_number as order_number" // 発注No
            + " , t1.expense_item as expense_item_code" // 費目 費目コード
            + " , (SELECT sq1.item1 FROM m_codmst sq1 " // 費目 費目名称
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid = '41' "
            + "      AND sq1.code1 = t1.expense_item) as expense_item_name"
            + " , t1.product_order_at as product_order_date" // 製品発注日
            + " , t3.dept_code as division_code" // 部門コード
            + " , (SELECT sq2.item2 FROM m_codmst sq1" // 事業部名
            + "      LEFT JOIN m_codmst sq2 "
            + "          ON sq2.deleted_at IS NULL "
            + "          AND sq2.mntflg IN ('1', '2', '') "
            + "          AND sq2.tblid = 'JG' "
            + "          AND sq2.code1 = sq1.item4"
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='02' "
            + "      AND sq1.code1 = t3.brand_code) as division_name"
            + " , (SELECT sq1.item1 FROM m_codmst sq1 " // アイテム名
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='03' "
            + "      AND sq1.code1 = t3.brand_code "
            + "      AND sq1.code2 = t3.item_code) as item_name"
            + " , (SELECT sq1.item2 FROM m_codmst sq1 " // 原産国
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='05' "
            + "      AND sq1.code1 = t3.coo_code) as country_of_origin"
            + " , t1.quantity as quantity" // 数量
            + " , t1.unit_price as unit_price" // 単価
            + " , t1.product_delivery_at as product_delivery_date" // 製品納期
            + " , t3.year as year" // 年度
            + " , t3.season_code as season" // シーズン
            + " , t1.retail_price as retail_price" // 上代
            + " , t3.product_name as product_name" // 品名
            + " , (SELECT sq1.item2 FROM m_codmst sq1 " // 製造担当
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='22' "
            + "      AND sq1.code1 = t3.mdf_staff_code) as mdf_staff"
            + " , (SELECT sq1.item2 FROM m_codmst sq1 " // 企画担当
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='22' "
            + "      AND sq1.code1 = t3.planner_code) as planning_staff"
            + " , (SELECT sq1.item2 FROM m_codmst sq1 " // パタンナー
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '') "
            + "      AND sq1.tblid='22' "
            + "      AND sq1.code1 = t3.pataner_code) as pataner"
            + " , t1.relation_number as relation_number" // 関連No
            + " , t1.attached_cost as attached_cost" // 附属代
            + " , t1.other_cost as other_cost" // その他原価
            + " , t1.product_cost as product_cost" // 製品原価
            + " , t1.application as application" // 適用
            + " , (SELECT sq2.item2 FROM m_codmst sq1" // 会社名
            + "      LEFT JOIN m_codmst sq2 "
            + "        ON sq2.deleted_at IS NULL "
            + "        AND sq2.mntflg IN ('1', '2', '')"
            + "        AND sq2.tblid='61' "
            + "        AND sq2.code1 = sq1.item3 "
            + "      WHERE sq1.deleted_at IS NULL "
            + "      AND sq1.mntflg IN ('1', '2', '')"
            + "      AND sq1.tblid='02' "
            + "      AND sq1.code1 = t3.brand_code ) as company_name"
            + " FROM"
            + "  t_order t1"
            + "  LEFT JOIN m_sirmst t2 ON t2.sire = t1.mdf_maker_code "
            + "  LEFT JOIN t_item t3 ON t3.id = t1.part_no_id "
            + " WHERE"
            + "  t1.id = :orderId"
            + " ORDER BY"
            + "  t1.id", nativeQuery = true)
    Optional<ExtendedTOrderPDFEntity> findByOrderId(@Param("orderId") BigInteger orderId);

}
