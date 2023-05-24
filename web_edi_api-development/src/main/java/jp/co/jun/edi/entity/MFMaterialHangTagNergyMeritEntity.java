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
 * The persistent class for the m_f_material_hang_tag_nergy_merit database table.
 *
 */
@Entity
@Table(name = "m_f_material_hang_tag_nergy_merit")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFMaterialHangTagNergyMeritEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 資材種別. */
    @Convert(converter = FukukitaruMasterMaterialTypeConverter.class)
    @Column(name = "material_type")
    private FukukitaruMasterMaterialType materialType;

    /** 資材種別名. */
    @Column(name = "material_type_name")
    private String materialTypeName;

    /** 資材コード. */
    @Column(name = "material_code")
    private String materialCode;

    /** 資材コード名. */
    @Column(name = "material_code_name")
    private String materialCodeName;

    /** 出荷単位. */
    @Column(name = "moq")
    private Integer moq;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

}
