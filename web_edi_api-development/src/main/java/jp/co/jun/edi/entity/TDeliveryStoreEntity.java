package jp.co.jun.edi.entity;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.AllocationTypeConverter;
import jp.co.jun.edi.type.AllocationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品得意先情報のEntity.
 */
@Entity
@Table(name = "t_delivery_store")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryStoreEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品明細ID. */
    @Column(name = "delivery_detail_id")
    private BigInteger deliveryDetailId;

    /** 店舗コード. */
    @Column(name = "store_code")
    private String storeCode;

    /** 店舗別配分率ID. */
    @Column(name = "store_distribution_ratio_id")
    private BigInteger storeDistributionRatioId;

    /** 店舗別配分率区分. */
    @Column(name = "store_distribution_ratio_type")
    private String storeDistributionRatioType;

    /** 店舗別配分率. */
    @Column(name = "store_distribution_ratio")
    private BigDecimal storeDistributionRatio;

    /** 配分順. */
    @Column(name = "distribution_sort")
    private Integer distributionSort;

    /** 配分区分. */
    @Column(name = "allocation_type")
    @Convert(converter = AllocationTypeConverter.class)
    private AllocationType allocationType;
}
