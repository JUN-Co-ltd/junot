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

import jp.co.jun.edi.entity.converter.SendMailTypeConverter;
import jp.co.jun.edi.type.SendMailType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * メール送信バッチのEntity.
 */
@Entity
@Table(name = "t_send_mail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TSendMailEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 送信状態. */
    @Column(name = "send_status")
    @Convert(converter = SendMailTypeConverter.class)
    private SendMailType sendStatus;

    /** 送信先メールアドレス. */
    @Column(name = "to_mail_address")
    private String toMailAddress;

    /** CCメールアドレス. */
    @Column(name = "cc_mail_address")
    private String ccMailAddress;

    /** BCCメールアドレス. */
    @Column(name = "bcc_mail_address")
    private String bccMailAddress;

    /** 送信元メールアドレス. */
    @Column(name = "from_mail_address")
    private String fromMailAddess;

    /** 件名. */
    @Column(name = "subject")
    private String subject;

    /** 本文. */
    @Column(name = "message_body")
    private String messageBody;
}
