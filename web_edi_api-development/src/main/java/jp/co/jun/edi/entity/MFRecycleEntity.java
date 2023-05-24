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
 * フクキタル用シールマスタ.
 *
 */
@Entity
@Table(name = "m_f_recycle")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFRecycleEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** シールコード. */
    @Column(name = "recycle_code")
    private String recycleCode;

    /** シール名称. */
    @Column(name = "recycle_name")
    private String recycleName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;



}
