package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入SkuFormのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseSkuFormModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード. */
    private String colorCode;
    /** サイズ. */
    private String size;
    /** 配分課フォーム値リスト. */
    private List<PurchaseDivisionFormModel> divisionFA;
}
