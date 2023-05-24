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
 * フクキタル用付記用語マスタ.
 *
 */
@Entity
@Table(name = "m_f_appendices_term")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFAppendicesTermEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;

    /** 付記用語コード. */
    @Column(name = "appendices_term_code")
    private String appendicesTermCode;

    /** 付記用語コード名. */
    @Column(name = "appendices_term_code_name")
    private String appendicesTermCodeName;

    /** 付記用語文章. */
    @Column(name = "appendices_term_sentence")
    private String appendicesTermSentence;

    /** 特徴. */
    @Column(name = "characteristic")
    private String characteristic;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

}
