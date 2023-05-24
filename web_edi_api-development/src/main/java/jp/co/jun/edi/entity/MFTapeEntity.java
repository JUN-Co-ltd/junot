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
 * フクキタル用テープ種類マスタ.
 *
 */
@Entity
@Table(name = "m_f_tape")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFTapeEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** テープ種類コード. */
    @Column(name = "tape_code")
    private String tapeCode;

    /** テープ名称. */
    @Column(name = "tape_name")
    private String tapeName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;



}
