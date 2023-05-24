package jp.co.jun.edi.repository.constants;

/**
 * Repository定数クラス.
 */
public final class DelischeConstants {

    // 発注Id件数取得
    public static final String SELECT_CNT_PHRASE_SQL = "SELECT COUNT(o.id) ";

    // 納品月度(納期)
    public static final String DELIVERY_AT_MONTHLY_SQL =
            "   CASE WHEN DATE_FORMAT(dd.correction_at,'%e') >= 21"
            + "      THEN CASE DATE_FORMAT(dd.correction_at,'%c')"
            + "                WHEN 12 THEN 1"
            + "                ELSE DATE_FORMAT(dd.correction_at,'%c') + 1"
            + "            END"
            + "      ELSE DATE_FORMAT(dd.correction_at,'%c')"
            + "  END ";

    // 納品月度(製品納期)
    public static final String PRODUCT_DELIVERY_AT_MONTHLY_SQL =
            "   CASE WHEN DATE_FORMAT(o.product_delivery_at,'%e') >= 21"
            + "      THEN CASE DATE_FORMAT(o.product_delivery_at,'%c')"
            + "                WHEN 12 THEN 1"
            + "                ELSE DATE_FORMAT(o.product_delivery_at,'%c') + 1"
            + "            END"
            + "      ELSE DATE_FORMAT(o.product_delivery_at,'%c')"
            + "  END ";

    // シーズン
    public static final String SEASON_SQL =
            "   CASE i.sub_season_code"
            + "      WHEN 1 THEN 'A1'"
            + "      WHEN 2 THEN 'A2'"
            + "      WHEN 5 THEN 'B1'"
            + "      WHEN 6 THEN 'B2'"
            + "      WHEN 9 THEN 'C'"
            + "      ELSE NULL"
            + "  END ";

    // 納期遅延
    public static final String LATE_DELIVERY_AT_SQL =
            "   o.product_delivery_at < dd.correction_at"
            + " AND ("
            + "      d.delivery_date_change_reason_id IN ('2', '9')"
            + "      OR d.delivery_date_change_reason_id IS NULL"
            + "     ) ";

    // 納期遅延件数
    public static final String LATE_DELIVERY_AT_CNT_SUB_QUERY =
            "   ("
            + "  SELECT COUNT(d.id)"
            + "    FROM t_delivery d"
            + "      INNER JOIN t_delivery_detail dd"
            + "              ON dd.delivery_id = d.id"
            + "             AND dd.deleted_at IS NULL"
            + "   WHERE d.order_id = o.id"
            + "     AND d.deleted_at IS NULL"
            + "     AND " + LATE_DELIVERY_AT_SQL
            + " ) ";

    // 納期遅延区分
    public static final String LATE_DELIVERY_AT_FLG_SQL =
            "   CASE WHEN " + LATE_DELIVERY_AT_SQL
            + "      THEN '1'"
            + "      ELSE '0'"
            + "  END ";

    // デリスケ納品依頼サブクエリGROUP BY句
    public static final String DELIVERY_REQUEST_GROUP_BY_PHRASE_SQL =
            " GROUP BY o.id"
            + "      , d.id";

    // 納品依頼数(発注)合計
    public static final String DELIVERY_LOT_ORDER_SUM_SUB_QUERY;

    // 納品依頼数合計
    public static final String DELIVERY_LOT_SUM_SUB_QUERY;

    // 入荷数(発注)合計
    public static final String ARRIVAL_LOT_ORDER_SUM_SUB_QUERY;

    // 入荷数合計
    public static final String ARRIVAL_LOT_SUM_SUB_QUERY;

    // 生産工程区分
    public static final String PRODUCTION_STATUS_SQL;

    // 製品発注数合計
    public static final String PRODUCT_ORDER_LOT_SUM_SUB_QUERY;

    // 売上数合計
    public static final String POS_SALES_QUANTITY_SUM_SUB_QUERY =
            "   ("
            + "  SELECT"
            + "    IFNULL(SUM(tps.quantity), 0)"
            + "  FROM"
            + "    t_pos_order tps"
            + "  WHERE"
            + "    tps.part_no = i.part_no"
            + "    AND tps.deleted_at IS NULL"
            + "  ) ";

    /**
     */
    private DelischeConstants() {
    };

    static {
        DELIVERY_LOT_ORDER_SUM_SUB_QUERY =
                "   ("
                + "  SELECT SUM(ds.delivery_lot) as delivery_lot_sum"
                + "    FROM t_delivery d"
                + "      INNER JOIN t_delivery_detail dd"
                + "              ON dd.delivery_id = d.id"
                + "             AND dd.deleted_at IS NULL"
                + "      INNER JOIN t_delivery_sku ds"
                + "              ON ds.delivery_detail_id = dd.id"
                + "             AND ds.deleted_at IS NULL"
                + "   WHERE d.order_id = o.id"
                + "     AND d.deleted_at IS NULL"
                + " ) ";

        DELIVERY_LOT_SUM_SUB_QUERY =
                "   ("
                + "  SELECT"
                + "    SUM(dl_ds.delivery_lot)"
                + "  FROM"
                + "    t_delivery_sku dl_ds"
                + "    INNER JOIN t_delivery_detail dl_dd"
                + "      ON dl_ds.delivery_detail_id = dl_dd.id"
                + "      AND dl_dd.deleted_at IS NULL"
                + "  WHERE"
                + "    dl_dd.delivery_id = d.id"
                + "    AND dl_ds.deleted_at IS NULL"
                + " ) ";

        ARRIVAL_LOT_ORDER_SUM_SUB_QUERY =
                "   ("
                + "  SELECT SUM(ds.arrival_lot) as arrival_lot_sum"
                + "    FROM t_delivery d"
                + "      INNER JOIN t_delivery_detail dd"
                + "              ON dd.delivery_id = d.id"
                + "             AND dd.deleted_at IS NULL"
                + "      INNER JOIN t_delivery_sku ds"
                + "              ON ds.delivery_detail_id = dd.id"
                + "             AND ds.deleted_at IS NULL"
                + "   WHERE d.order_id = o.id"
                + "     AND d.deleted_at IS NULL"
                + " ) ";

        ARRIVAL_LOT_SUM_SUB_QUERY =
                "   ("
                + "  SELECT"
                + "    SUM(al_ds.arrival_lot)"
                + "  FROM"
                + "    t_delivery_sku al_ds"
                + "    INNER JOIN t_delivery_detail al_dd"
                + "      ON al_ds.delivery_detail_id = al_dd.id"
                + "      AND al_dd.deleted_at IS NULL"
                + "  WHERE"
                + "    al_dd.delivery_id = d.id"
                + "    AND al_ds.deleted_at IS NULL"
                + " ) ";

        PRODUCTION_STATUS_SQL =
                "   CASE WHEN ps.sample_completion_at < ps.sample_completion_fix_at"
                + "        OR ps.specification_at < ps.specification_fix_at"
                + "        OR ps.texture_arrival_at < ps.texture_arrival_fix_at"
                + "        OR ps.attachment_arrival_at < ps.attachment_arrival_fix_at"
                + "        OR ps.completion_at < ps.completion_fix_at"
                + "        OR ps.sew_inspection_at < ps.sew_inspection_fix_at"
                + "        OR ps.inspection_at < ps.inspection_fix_at"
                + "        OR ps.leave_port_at < ps.leave_port_fix_at"
                + "        OR ps.enter_port_at < ps.enter_port_fix_at"
                + "        OR ps.customs_clearance_at < ps.customs_clearance_fix_at"
                + "        OR ps.dista_arrival_at < ps.dista_arrival_fix_at"
                + "      THEN '1'"
                + "      WHEN ps.sample_completion_at IS NULL"
                + "       AND ps.sample_completion_fix_at IS NULL"
                + "       AND ps.specification_at IS NULL"
                + "       AND ps.specification_fix_at IS NULL"
                + "       AND ps.texture_arrival_at IS NULL"
                + "       AND ps.texture_arrival_fix_at IS NULL"
                + "       AND ps.attachment_arrival_at IS NULL"
                + "       AND ps.attachment_arrival_fix_at IS NULL"
                + "       AND ps.completion_at IS NULL"
                + "       AND ps.completion_fix_at IS NULL"
                + "       AND ps.sew_inspection_at IS NULL"
                + "       AND ps.sew_inspection_fix_at IS NULL"
                + "       AND ps.inspection_at IS NULL"
                + "       AND ps.inspection_fix_at IS NULL"
                + "       AND ps.leave_port_at IS NULL"
                + "       AND ps.leave_port_fix_at IS NULL"
                + "       AND ps.enter_port_at IS NULL"
                + "       AND ps.enter_port_fix_at IS NULL"
                + "       AND ps.customs_clearance_at IS NULL"
                + "       AND ps.customs_clearance_fix_at IS NULL"
                + "       AND ps.dista_arrival_at IS NULL"
                + "       AND ps.dista_arrival_fix_at IS NULL"
                + "      THEN '2'"
                + "      ELSE '0'"
                + "  END ";

        PRODUCT_ORDER_LOT_SUM_SUB_QUERY =
                "   ("
                + "  SELECT"
                + "    SUM(pol_os.product_order_lot)"
                + "  FROM"
                + "    t_order_sku pol_os"
                + "  WHERE"
                + "    pol_os.order_id = d.order_id"
                + "    AND pol_os.deleted_at IS NULL"
                + "    AND EXISTS ("
                + "      SELECT"
                + "        pol_ds.id"
                + "      FROM"
                + "        t_delivery_sku pol_ds"
                + "        INNER JOIN t_delivery_detail pol_dd"
                + "          ON pol_ds.delivery_detail_id = pol_dd.id"
                + "          AND pol_dd.deleted_at IS NULL"
                + "      WHERE"
                + "        pol_dd.delivery_id = d.id"
                + "        AND pol_ds.color_code = pol_os.color_code"
                + "        AND pol_ds.size = pol_os.size"
                + "    )"
                + " ) ";
    }
}
