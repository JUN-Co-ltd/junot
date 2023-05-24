package jp.co.jun.edi.component;

import org.springframework.stereotype.Component;

/**
 * 出荷関連のコンポーネント.
 */
@Component
public class ShipmentComponent extends GenericComponent {

    private static final int OLD_LOGISTIC_CODE_START_IDX = 4;
    private static final int OLD_LOGISTIC_CODE_END_IDX = 6;

    private static final int LOGISTIC_CODE_START_IDX = 4;
    private static final int LOGISTIC_CODE_END_IDX = 5;

    private static final int SHPCD_LEN = 8;

    /**
     * 2桁の物流コードを取得.
     * (新しい物流コードは１桁).
     * テーブルによって、入荷場所・出荷場所・物流コードと表現されている.
     * @param shpcd 店舗コード
     * @return 入荷場所
     */
    public String extraxtOldLogisticsCode(final String shpcd) {
        if (shpcd == null || shpcd.length() != SHPCD_LEN) {
            return null;
        }
        return shpcd.substring(OLD_LOGISTIC_CODE_START_IDX, OLD_LOGISTIC_CODE_END_IDX);
    }

    /**
     * 1桁の倉庫コードを取得.
     * テーブルによって、入荷場所・出荷場所・物流コードと表現されている.
     * @param shpcd 店舗コード
     * @return 入荷場所
     */
    public String extraxtLogisticsCode(final String shpcd) {
        if (shpcd == null || shpcd.length() != SHPCD_LEN) {
            return null;
        }
        return shpcd.substring(LOGISTIC_CODE_START_IDX, LOGISTIC_CODE_END_IDX);
    }
}
