package jp.co.jun.edi.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品情報のEntity.
 */
@Entity
@Table(name = "t_delivery")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryEntity extends GenericEntity {
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
    // 本来はApprovalType型にすべきですが既に多くの箇所でString前提で作成されているのでStringのままにしてください
    @Column(name = "delivery_approve_status")
    private String deliveryApproveStatus;

    /** 承認日. */
    @Column(name = "delivery_approve_at")
    private Date deliveryApproveAt;

    /** 配分率区分. */
    @Column(name = "distribution_ratio_type")
    private String distributionRatioType;

    /** メモ. */
    private String memo;

    /** 納期変更理由ID. */
    @Column(name = "delivery_date_change_reason_id")
    private Integer deliveryDateChangeReasonId;

    /** 納品変更理由詳細. */
    @Column(name = "delivery_date_change_reason_detail")
    private String deliveryDateChangeReasonDetail;

    /** B級品区分. */
    @Column(name = "non_conforming_product_type")
    private boolean nonConformingProductType;

    /** B級品単価. */
    @Column(name = "non_conforming_product_unit_price")
    private BigDecimal nonConformingProductUnitPrice;

    /** SQロックフラグ. */
    @Column(name = "sq_lock_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType sqLockFlg = BooleanType.FALSE;

    /** SQロックユーザーID. */
    @Column(name = "sq_lock_user_id")
    private BigInteger sqLockUserId;
}
