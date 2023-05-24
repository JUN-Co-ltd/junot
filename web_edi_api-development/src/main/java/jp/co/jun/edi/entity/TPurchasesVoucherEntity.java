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
import lombok.experimental.Accessors;

//PRD_0134 #10654 add JEF start
/**
 * 仕入伝票管理のEntity.
 */
@Table(name = "t_purchases_voucher")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Accessors(chain=true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TPurchasesVoucherEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 仕入伝票行. */
    @Column(name = "purchase_voucher_line")
    private Integer purchaseVoucherLine;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 状態. */
    @Convert(converter = SendMailStatusTypeConverter.class)
    @Column(name = "status")
    private SendMailStatusType status;

    /** 仕入先コード. */
    @Column(name = "supplier_code")
    private String supplierCode;

}
//PRD_0134 #10654 add JEF end