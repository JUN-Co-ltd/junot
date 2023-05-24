package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * パーツマスタ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** アイテム. */
    private String itemCode;

    /** パーツ名. */
    private String partsName;

    /** ソート順. */
    private int sortOrder;
}
