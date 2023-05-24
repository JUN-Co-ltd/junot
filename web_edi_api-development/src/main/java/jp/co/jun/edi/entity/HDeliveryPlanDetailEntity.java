package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

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
 * 納品予定明細履歴のEntity.
 */
@Entity
@Table(name = "h_delivery_plan_detail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class HDeliveryPlanDetailEntity extends GenericEntity {
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

    /** 納品予定日. */
    @Column(name = "delivery_plan_at")
    private Date deliveryPlanAt;
}
