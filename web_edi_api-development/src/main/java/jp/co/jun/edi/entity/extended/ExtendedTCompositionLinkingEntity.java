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
 * 拡張組成情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTCompositionLinkingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** パーツ. */
    @Column(name = "parts")
    private String parts;

    /** 組成コード. */
    @Column(name = "composition_code")
    private String compositionCode;

    /** 組成. */
    @Column(name = "composition")
    private String composition;

    /** 率. */
    @Column(name = "percent")
    private Integer percent;
}
