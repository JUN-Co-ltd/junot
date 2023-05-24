package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 品番・商品一括登録Excel操作用プロパティ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkRegistItemPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int ITEM_MAX_SIZE = 200;
    private static final int ITEM_INITIAL_CAPACITY = 100;
    private static final int SKU_JAN_MAX_SIZE = 1000;
    private static final int SKU_JAN_INITIAL_CAPACITY = 5;
    private static final int SKU_UPC_MAX_SIZE = 1000;
    private static final int SKU_UPC_INITIAL_CAPACITY = 5;
    private static final int COMPOSITION_MAX_SIZE = 10000;
    private static final int COMPOSITION_INITIAL_CAPACITY = 5;
    private static final int ITEM_ERROR_COUNT = 200;

    /** 「商品_品番取込用」シートの最大レコード数. */
    private int itemMaxSize = ITEM_MAX_SIZE;

    /** 「商品_品番取込用」シートのリストの初期サイズ. */
    private int itemInitialCapacity = ITEM_INITIAL_CAPACITY;

    /** 「SKU取込用(JAN)」シートの最大レコード数. */
    private int skuJanMaxSize = SKU_JAN_MAX_SIZE;

    /** 「SKU取込用(JAN)」シートのリストの初期サイズ. */
    private int skuJanInitialCapacity = SKU_JAN_INITIAL_CAPACITY;

    /** 「SKU取込用(UPC)」シートの最大レコード数. */
    private int skuUpcMaxSize = SKU_UPC_MAX_SIZE;

    /** 「SKU取込用(UPC)」シートのリストの初期サイズ. */
    private int skuUpcInitialCapacity = SKU_UPC_INITIAL_CAPACITY;

    /** 「組成取込用」シートの最大レコード数. */
    private int compositionMaxSize = COMPOSITION_MAX_SIZE;

    /** 「組成取込用」シートのリストの初期サイズ. */
    private int compositionInitialCapacity = COMPOSITION_INITIAL_CAPACITY;

    /** エラーの最大数. */
    private int itemErrorCount = ITEM_ERROR_COUNT;
}
