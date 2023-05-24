package jp.co.jun.edi.model.mail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品依頼登録時のメール送信先用データModel.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryRequestRegistSendModel extends GetMailAdressCommonModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 生産メーカー名. */
    private String mdfMakerName;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 発注数. */
    private BigDecimal quantity;

    /** 納期. */
    private Date productDeliveryAt;

    /** 撮影納期. */
    private Date photoDeliveryAt;

    /** 縫検納期. */
    private Date sewingDeliveryAt;

    /** 製品納期. */
    private Date deliveryAt;

    /** 納品数. */
    private BigDecimal allDeliveredLot;
}

