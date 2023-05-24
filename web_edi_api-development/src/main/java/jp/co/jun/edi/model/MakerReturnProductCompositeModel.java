package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.validation.group.UpdateValidationGroup;
import lombok.Data;

/**
 * メーカー返品商品情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnProductCompositeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** メーカー返品ID. */
    @NotNull(groups = { UpdateValidationGroup.class })
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** カラーコード. */
    @NotBlank(groups = { Default.class })
    private String colorCode;

    /** サイズ. */
    @NotBlank(groups = { Default.class })
    private String size;

    /** 発注ID. */
    @NotNull(groups = { Default.class })
    private BigInteger orderId;

    /** 発注番号. */
    private BigInteger orderNumber;

    /** 返品数量. */
    @NotNull(groups = { Default.class })
    @Min(value = 1, groups = Default.class)
    private Integer returnLot;

    /** 品名. */
    private String productName;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date productOrderAt;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 下代(発注の単価). */
    private BigDecimal unitPrice;

    /** 最新の単価(品番情報のその他原価). */
    private BigDecimal otherCost;

    /** 在庫数. */
    private Integer stockLot;
}
