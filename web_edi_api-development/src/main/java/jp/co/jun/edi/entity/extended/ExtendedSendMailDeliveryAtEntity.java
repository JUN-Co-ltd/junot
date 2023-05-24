package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * メール送信用納期データのEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedSendMailDeliveryAtEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 撮影納期. */
    @Column(name = "photo_delivery_at")
    private Date photoDeliveryAt;

    /** 縫検納期. */
    @Column(name = "sewing_delivery_at")
    private Date sewingDeliveryAt;

    /** 製品納期. */
    @Column(name = "delivery_at")
    private Date deliveryAt;
}
