package jp.co.jun.edi.model.mail;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メール送信先を取得するための共通Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMailAdressCommonModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 企画担当. */
    private String plannerCode;

    /** 製造担当. */
    private String mdfStaffCode;

    /** パターンナー. */
    private String patanerCode;

    /** 生産メーカー担当. */
    private BigInteger mdfMakerStaffId;

    /** 生産メーカーコード. */
    private String mdfMakerCode;
}
