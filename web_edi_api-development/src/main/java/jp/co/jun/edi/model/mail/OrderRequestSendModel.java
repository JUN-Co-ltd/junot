package jp.co.jun.edi.model.mail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 発注確定時のメール送信先用データModel.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequestSendModel extends GetMailAdressCommonModel implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /** 納期. */
    private Date productDeliveryAt;

    /** 発注ID. */
    private BigInteger orderId;

    /** URL. */
    private String url;

    /** 件名接頭辞. */
    private String subjectPrefix;
}

