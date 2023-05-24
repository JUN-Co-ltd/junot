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
 * 納品SKUのEntity.
 */
@Entity
@Table(name = "t_delivery_sku")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliverySkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品明細ID. */
    @Column(name = "delivery_detail_id")
    private BigInteger deliveryDetailId;

    /** 納品依頼No. */
    @Column(name = "delivery_request_number")
    private String deliveryRequestNumber;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 納品数量. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 入荷数量. */
    @Column(name = "arrival_lot")
    private int arrivalLot;
}
