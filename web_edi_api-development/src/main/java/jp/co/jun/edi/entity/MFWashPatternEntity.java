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
 * The persistent class for the m_f_wash_pattern database table.
 *
 */
@Entity
@Table(name = "m_f_wash_pattern")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFWashPatternEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;


    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

    /** 洗濯マークコード. */
    @Column(name = "wash_pattern_code")
    private String washPatternCode;

    /** 洗濯マーク名称. */
    @Column(name = "wash_pattern_name")
    private String washPatternName;

}
