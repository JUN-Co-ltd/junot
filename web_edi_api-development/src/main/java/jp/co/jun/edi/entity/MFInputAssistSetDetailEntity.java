package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BigIntegerArrayTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterMaterialTypeConverter;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the t_f_item database table.
 *
 */
@Entity
@Table(name = "m_f_input_assist_set_detail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFInputAssistSetDetailEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 入力補助セットID. */
    @Column(name = "assist_set_id")
    private BigInteger assistSetId;

    /** 資材種別. */
    @Convert(converter = FukukitaruMasterMaterialTypeConverter.class)
    @Column(name = "material_type")
    private FukukitaruMasterMaterialType materialType;

    @Convert(converter = BigIntegerArrayTypeConverter.class)
    @Column(name = "material_id_list")
    private List<BigInteger> materialIdList;
}
