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
 * The persistent class for the t_delivery_official_send_mail database table.
 *
 */
@Entity
@Table(name = "t_delivery_official_send_mail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryOfficialSendMailEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 生産メーカー. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 生産工場. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 状態. */
    @Column(name = "status")
    @Convert(converter = SendMailStatusTypeConverter.class)
    private SendMailStatusType status;

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

    /** PDFのみ生成する. */
    @Column(name = "created_only_pdf")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType createdOnlyPdf;

}
