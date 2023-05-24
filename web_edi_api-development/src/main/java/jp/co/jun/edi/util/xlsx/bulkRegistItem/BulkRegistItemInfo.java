package jp.co.jun.edi.util.xlsx.bulkRegistItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * 品番・商品一括登録用の情報.
 */
@Data
@Builder
public class BulkRegistItemInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番・商品. */
    private final BulkRegistItemSheetInfo item;

    /** SKU(JAN). */
    private final BulkRegistItemSheetInfo skuJan;

    /** SKU(UPC). */
    private final BulkRegistItemSheetInfo skuUpc;

    /** 組成. */
    private final BulkRegistItemSheetInfo composition;

    /**
     * シート情報の取得.
     *
     * @return シート情報のリスト
     */
    public List<BulkRegistItemSheetInfo> getSheetInfos() {
        return toSheetInfos(this);
    }

    /**
     * シート情報の取得.
     *
     * @param model {@link BulkRegistItemInfo} instance
     * @return シート情報のリスト
     */
    private static List<BulkRegistItemSheetInfo> toSheetInfos(final BulkRegistItemInfo model) {
        return new ArrayList<BulkRegistItemSheetInfo>() {
            private static final long serialVersionUID = 1L;

            {
                add(model.getItem());
                add(model.getSkuJan());
                add(model.getSkuUpc());
                add(model.getComposition());
            }
        };
    }
}
