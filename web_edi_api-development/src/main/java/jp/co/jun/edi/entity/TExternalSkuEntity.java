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
 * 外部SKU情報のEntity.
 */
@Entity
@Table(name = "t_external_sku")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TExternalSkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** SKUID. */
    @Column(name = "sku_id")
    private BigInteger skuId;

    /** 他社品番. */
    @Column(name = "external_part_no")
    private String externalPartNo;

    /** 他社カラーコード. */
    @Column(name = "external_color_code")
    private String externalColorCode;

    /** 他社サイズ. */
    @Column(name = "external_size")
    private String externalSize;
}
