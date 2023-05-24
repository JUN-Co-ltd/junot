package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 納品出荷ファイルPDFフォーマットのヘッダ用Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedDirectDeliveryPDFHeaderEntity {

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 郵便番号. */
    @Column(name = "yubin")
    private String yubin;

    /** 住所1. */
    @Column(name = "address1")
    private String address1;

    /** 住所2. */
    @Column(name = "address2")
    private String address2;

    /** 住所3. */
    @Column(name = "address3")
    private String address3;

    /** 御得意先様Co. */
    @Column(name = "shop_code")
    private String shopCode;

    /** 御得意先名. */
    @Column(name = "shop_name")
    private String shopName;
}
