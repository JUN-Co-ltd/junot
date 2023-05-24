package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
/**
 * 納品情報のEntity.
 */
@Entity
@Data
public class DeliveryCompositeEntity implements Serializable {
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

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 最終納品ステータス. */
    @Column(name = "last_delivery_status")
    private String lastDeliveryStatus;

    /** 承認ステータス. */
    @Column(name = "delivery_approve_status")
    private String deliveryApproveStatus;

    /** 承認日. */
    @Column(name = "delivery_approve_at")
    private Date deliveryApproveAt;

    /** 納品依頼日. */
    @Column(name = "delivery_request_at")
    private Date deliveryRequestAt;

    /** 修正納期. */
    @Column(name = "correction_at")
    private Date correctionAt;

    /** 納品依頼数合計. */
    @Column(name = "sum_delivery_lot")
    private int sumDeliveryLot;
}
