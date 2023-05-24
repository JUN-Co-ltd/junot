package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品依頼回数採番マスタのEntity.
 */
@Entity
@Table(name = "m_delivery_count_number")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MDeliveryCountNumberEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;
}
