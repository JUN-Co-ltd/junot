package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 売上情報明細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosOrderDetailModel implements Serializable {
	private static final long serialVersionUID = 1L;

    /** 店舗コード */
    private String storeCode;

    /** 品番 */
    private String partNo;

    /** カラーコード */
    private String colorCode;

    /** サイズコード */
    private String sizeCode;

    /** 売上点数 */
    private int salesScore;

}
