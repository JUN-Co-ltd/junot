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

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * アイテム別パーツマスタのEntity.
 */
@Entity
@Table(name = "m_item_parts")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MItemPartsEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** アイテム. */
    @Column(name = "item_code")
    private String itemCode;

    /** パーツ名. */
    @Column(name = "parts_name")
    private String partsName;

    /** ソート順. */
    @Column(name = "sort_order")
    private int sortOrder;

    /**
     * その他フラグ.
     * false:パーツその他でない(0)、true:パーツその他(1)
     */
    @Convert(converter = BooleanTypeConverter.class)
    @Column(name = "other_parts_flg")
    private BooleanType otherPartsFlg;
}
