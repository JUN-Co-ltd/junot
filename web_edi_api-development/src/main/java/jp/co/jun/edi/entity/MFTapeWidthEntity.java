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
 * フクキタル用テープ巾マスタ.
 *
 */
@Entity
@Table(name = "m_f_tape_width")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFTapeWidthEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** テープ巾コード. */
    @Column(name = "tape_width_code")
    private String tapeWidthCode;

    /** テープ巾名称. */
    @Column(name = "tape_width_name")
    private String tapeWidthName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;


}
