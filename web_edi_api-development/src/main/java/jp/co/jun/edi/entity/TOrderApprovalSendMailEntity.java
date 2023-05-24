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

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.SendMailStatusTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.SendMailStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 発注書PDF発行[即時]メール送信管理のEntity.
 */
@Table(name = "t_order_approval_send_mail")
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class TOrderApprovalSendMailEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 状態. */
    @Convert(converter = SendMailStatusTypeConverter.class)
    @Column(name = "status")
    private SendMailStatusType status;

    /** PDFのみ生成する. */
    @Convert(converter = BooleanTypeConverter.class)
    @Column(name = "created_only_pdf")
    private BooleanType createdOnlyPdf;

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
    private String fromMailAddress;

    /** 件名. */
    @Column(name = "subject")
    private String subject;

    /** 本文. */
    @Column(name = "message_body")
    private String messageBody;

}
