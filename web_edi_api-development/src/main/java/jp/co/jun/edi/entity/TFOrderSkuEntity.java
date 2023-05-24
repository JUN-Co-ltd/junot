package jp.co.jun.edi.entity;

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

import jp.co.jun.edi.entity.converter.FukukitaruMasterMaterialTypeConverter;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the t_f_order_sku database table.
 *
 */
@Entity
@Table(name = "t_f_order_sku")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TFOrderSkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** フクキタル発注ID. */
    @Column(name = "f_order_id")
    private BigInteger fOrderId;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 資材情報ID. */
    @Column(name = "material_id")
    private BigInteger materialId;

    /** 資材数量. */
    @Column(name = "order_lot")
    private int orderLot;

    /** 資材種別. */
    @Convert(converter = FukukitaruMasterMaterialTypeConverter.class)
    @Column(name = "material_type")
    private FukukitaruMasterMaterialType materialType;

    /** 並び順. */
    @Column(name = "sort_order")
    private Integer sortOrder;

}
