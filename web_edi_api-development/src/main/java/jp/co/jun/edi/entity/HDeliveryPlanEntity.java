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
 * 納品予定履歴のEntity.
 */
@Entity
@Table(name = "h_delivery_plan")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class HDeliveryPlanEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** 履歴ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger historyId;

    /** ID. */
    private BigInteger id;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 登録済ステータス. */
    @Column(name = "entry_status")
    private int entryStatus;

    /** メモ. */
    private String memo;
}
