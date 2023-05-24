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
 * 納品予定裁断履歴のEntity.
 */
@Entity
@Table(name = "h_delivery_plan_cut")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class HDeliveryPlanCutEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** 履歴ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger historyId;

    /** ID. */
    private BigInteger id;

    /** 納品予定ID. */
    @Column(name = "delivery_plan_id")
    private BigInteger deliveryPlanId;

    /** サイズ. */
    private String size;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 裁断数. */
    @Column(name = "delivery_plan_cut_lot")
    private int deliveryPlanCutLot;
}
