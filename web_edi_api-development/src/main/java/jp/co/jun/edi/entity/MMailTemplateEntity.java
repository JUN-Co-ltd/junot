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

import jp.co.jun.edi.entity.converter.MMailCodeTypeConverter;
import jp.co.jun.edi.entity.converter.MMailStatusTypeConverter;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.MMailStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * メールテンプレートマスタのEntity.
 */
@Entity
@Table(name = "m_mail_template")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MMailTemplateEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** メール分類. */
    @Column(name = "mail_code")
    @Convert(converter = MMailCodeTypeConverter.class)
    private MMailCodeType mailCode;

    /** ステータス. */
    @Column(name = "status_type")
    @Convert(converter = MMailStatusTypeConverter.class)
    private MMailStatusType statusType;

    /** 言語. */
    @Column(name = "language")
    private String language;

    /** タイトル. */
    @Column(name = "title")
    private String title;

    /** 本文. */
    @Column(name = "content")
    private String content;
}
