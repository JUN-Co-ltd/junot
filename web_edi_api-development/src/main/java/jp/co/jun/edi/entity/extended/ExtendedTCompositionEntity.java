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
 * 拡張組成情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTCompositionEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 連番. */
    @Column(name = "serial_number")
    private Integer serialNumber;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** パーツ. */
    @Column(name = "parts_code")
    private String partsCode;

    /** パーツ名. */
    @Column(name = "parts_name")
    private String partsName;

    /** 組成. */
    @Column(name = "composition_code")
    private String compositionCode;

    /** 率. */
    @Column(name = "percent")
    private Integer percent;

    /**
     * 色名称.
     */
    @Column(name = "color_name")
    private String colorName;

    /**
     * 組成名称.
     */
    @Column(name = "composition_name")
    private String compositionName;
}
