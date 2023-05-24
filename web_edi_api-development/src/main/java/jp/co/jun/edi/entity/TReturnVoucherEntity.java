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

import jp.co.jun.edi.entity.converter.SendMailStatusTypeConverter;
import jp.co.jun.edi.type.SendMailStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 返品伝票管理のEntity.
 */
@Table(name = "t_returns_voucher")
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
@Data
public class TReturnVoucherEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 状態. */
    @Convert(converter = SendMailStatusTypeConverter.class)
    @Column(name = "status")
    private SendMailStatusType status;

}
