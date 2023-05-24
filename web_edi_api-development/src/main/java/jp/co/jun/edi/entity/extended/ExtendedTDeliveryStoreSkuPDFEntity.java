package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 納品得意先SKU情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTDeliveryStoreSkuPDFEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品得意先ID. */
    @Column(name = "delivery_store_id")
    private BigInteger deliveryStoreId;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 納品数量. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 出荷確定数. */
    @Column(name = "arrival_lot")
    private int arrivalLot;

}
