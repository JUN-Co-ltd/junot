package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.constants.RegexpConstants;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import lombok.Data;

/**
 * 仕入のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** データ種別. */
    private PurchaseDataType dataType;

    /** 仕入区分. */
    private PurchaseType purchaseType;

    /** 入荷場所. */
    private String arrivalPlace;

    /** 入荷店舗. */
    @NotBlank(groups = { Default.class })
    private String arrivalShop;

    /** 仕入先. */
    private String supplierCode;

    /** 製品工場. */
    private String mdfMakerFactoryCode;

    /** 入荷日. */
    @NotNull(groups = { Default.class })
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date arrivalAt;

    /** 計上日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date recordAt;

    /** 仕入相手伝票No. */
    @Pattern(regexp = RegexpConstants.MAKER_VOUCHER_NUMBER, groups = Default.class)
    private String makerVoucherNumber;

    /** 仕入伝票No. */
    private String purchaseVoucherNumber;

    /** 仕入伝票行. */
    private Integer purchaseVoucherLine;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 仕入SKUリスト. */
    @Valid
    private List<PurchaseSkuModel> purchaseSkus;

    /** 良品・不用品区分. */
    private BooleanType nonConformingProductType;

    /** 指示番号. */
    private String instructNumber;

    /** 指示番号行. */
    private Integer instructNumberLine;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 引取回数(納品明細の納品回数). */
    private Integer purchaseCount;

    /** 仕入単価. */
    private Integer purchaseUnitPrice;

    /** 納品ID. */
    private BigInteger deliveryId;
}
