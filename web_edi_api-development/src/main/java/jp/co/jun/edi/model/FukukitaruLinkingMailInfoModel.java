package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル連携メール情報用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruLinkingMailInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** ID. */
    private BigInteger id;

    /** オーダー識別コード. */
    private String orderCode;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 生産メーカー. */
    private String sire;

}
