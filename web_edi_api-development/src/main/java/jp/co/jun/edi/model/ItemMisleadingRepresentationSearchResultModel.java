package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MSirmstYugaikbnType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * 優良誤認検査承認一覧情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemMisleadingRepresentationSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番ID. */
    private BigInteger id;

    /** 品番. */
    private String partNo;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 品名. */
    private String productName;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 発注数. */
    private BigInteger quantity;

    /** 製造担当者コード. */
    private String mdfStaffCode;

    /** 製造担当者名. */
    private String mdfStaffName;

    /** 原産国コード. */
    private String cooCode;

    /** 原産国名. */
    private String cooName;

    /** 優良誤認承認区分（国）. */
    private QualityApprovalType qualityCooStatus;

    /** 組成コード. */
    private String compositionCode;

    /** 組成名. */
    private String compositionName;

    /** 優良誤認承認区分（組成）. */
    private QualityApprovalType qualityCompositionStatus;

    /** 有害区分. */
    private MSirmstYugaikbnType yugaikbn;

    /** 優良誤認承認区分（有害物質）. */
    private QualityApprovalType qualityHarmfulStatus;

    /** 検査承認日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date approvalAt;
}
