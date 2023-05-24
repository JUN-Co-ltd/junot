package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * メーカー返品情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 伝票番号. */
    private String voucherNumber;

    /** 伝票番号行. */
    private Integer voucherLine;

    /** 管理No. */
    private String manageNumber;

    /** 店舗コード. */
    @NotBlank(groups = { Default.class })
    private String shpcd;

    /** 店舗名. */
    private String shopName;

    /** ディスタコード. */
    @NotBlank(groups = { Default.class })
    private String distaCode;

    /** 物流コード. */
    private String logisticsCode;

    /** 仕入先コード. */
    @NotBlank(groups = { Default.class })
    private String supplierCode;

    /** 仕入先名称. */
    private String supplierName;

    /** 返品日. */
    @NotNull(groups = { Default.class })
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date returnAt;

    /** 製造担当コード. */
    @NotBlank(groups = { Default.class })
    private String mdfStaffCode;

    /** 製造担当名称. */
    private String mdfStaffName;

    /** 摘要. */
    private String memo;

    /** LG送信区分. */
    private LgSendType lgSendType;

    /** 数量. */
    private String returnLot;

    /** 単価. */
    private String unitPrice;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注番号. */
    private String orderNumber;

    /** 伝票入力日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date createdAt;

    /** メーカー返品商品リスト. */
    @Valid
    @NotNull(groups = { Default.class })
    private List<MakerReturnProductCompositeModel> makerReturnProducts;

    /** メーカー返品ファイルID. */
    private BigInteger makerReturnFileNoId;
}
