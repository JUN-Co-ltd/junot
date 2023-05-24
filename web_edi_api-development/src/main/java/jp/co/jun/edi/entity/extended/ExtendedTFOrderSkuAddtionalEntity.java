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
 * 拡張フクキタル発注SKU情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTFOrderSkuAddtionalEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 資材種類名. */
    @Column(name = "material_type_name")
    private String materialTypeName;

    /** 資材コード. */
    @Column(name = "material_code")
    private String materialCode;

    /** 資材数量. */
    @Column(name = "order_lot")
    private Integer orderLot;

}
