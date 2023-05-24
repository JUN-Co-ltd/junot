package jp.co.jun.edi.util.xlsx.bulkRegistItem;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/**
 * 品番・商品一括登録用のシートの情報.
 */
@Data
@Builder
public class BulkRegistItemSheetInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** シート名. */
    private final String sheetName;

    /** レコード数オーバー. */
    private final boolean over;

    /** 最大レコード数. */
    private final int maxSize;
}
