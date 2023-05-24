package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 優良誤認検索結果返却用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationSearchResultModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 最新発注ID. */
    private BigInteger latestOrderId;

    /** 最新発注No. */
    private BigInteger latestOrderNumber;

    /** 最新発注数. */
    private BigDecimal latestQuantity;

    /** 生産メーカー. */
    private String mdfMakerCode;

    /** 生産メーカー名称. */
    private String mdfMakerName;

    /** 原産国コード. */
    private String cooCode;

    /** 原産国名称. */
    private String cooName;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 製造担当コード. */
    private String mdfStaffCode;

    /** 製造担当名称. */
    private String mdfStaffName;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 優良誤認区分. */
    private boolean misleadingRepresentation;

    /** 優良誤認承認区分（組成）. */
    private int qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private int qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private int qualityHarmfulStatus;

    /** 最新承認日. */
    private Date latestApprovalAt;

    /** 代表組成コード. */
    private String representationCompositionCode;

    /** 代表組成名. */
    private String representationCompositionName;

}
