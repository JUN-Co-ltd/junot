package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張フクキタル発注SKU情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTFOrderSkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** フクキタル発注ID. */
    @Column(name = "f_order_id")
    private BigInteger fOrderId;

    /** カラーコード. */
    @Column(name = "colorCode")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 資材ID. */
    @Column(name = "material_id")
    private BigInteger materialId;

    /** 資材数量. */
    @Column(name = "order_lot")
    private Integer orderLot;

    /** 資材種類. */
    @Column(name = "material_type")
    private Integer materialType;

    /** 資材種類名. */
    @Column(name = "material_type_name")
    private String materialTypeName;

    /** 資材コード. */
    @Column(name = "material_code")
    private String materialCode;

    /** 資材コード名. */
    @Column(name = "material_code_name")
    private String materialCodeName;

    /** 並び順. */
    @Column(name = "sort_order")
    private Integer sortOrder;
}
