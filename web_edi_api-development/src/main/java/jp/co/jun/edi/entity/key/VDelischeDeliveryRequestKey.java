package jp.co.jun.edi.entity.key;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * デリスケ納品依頼情報ViewのKey.
 */
@Embeddable
@Data
public class VDelischeDeliveryRequestKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private Integer deliveryCount;

    /** 納期. */
    @Column(name = "delivery_at")
    private Date deliveryAt;
}
