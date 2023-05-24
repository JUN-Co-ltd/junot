package jp.co.jun.edi.model.mail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品依頼承認時のメール送信先用データModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverySendMailModel  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** タイトル.接頭語. */
    private String subjectPrefix;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 生産メーカーコード. */
    private String mdfMakerCode;

    /** 生産メーカー名. */
    private String mdfMakerName;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 発注数. */
    private BigDecimal quantity;

    /** 納品日. */
    private Date deliveryAt;

    /** 納品数. */
    private BigDecimal deliveryQuantity;

    /** URL. */
    private String url;

    /** 納品ID. */
    private BigInteger deliveryId;

}

