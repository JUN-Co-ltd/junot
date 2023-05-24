package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入SkuのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 仕入配分課リスト. */
    @Valid
    private List<PurchaseDivisionModel> purchaseDivisions;
}
