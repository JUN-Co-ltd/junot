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
 * メーカー返品ファイル情報のEntity.
 */
@Entity
@Table(name = "t_maker_return_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TMakerReturnFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

}
