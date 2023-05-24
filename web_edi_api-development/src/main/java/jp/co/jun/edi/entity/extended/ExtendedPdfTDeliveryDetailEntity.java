package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
/**
 * ExtendedPdfTDeliveryDetailEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedPdfTDeliveryDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品詳細ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品依頼No. */
    @Column(name = "delivery_request_number")
    private String deliveryRequestNumber;

    /** 納品No. */
    @Column(name = "delivery_number")
    private String deliveryNumber;

    /** 修正納期. */
    @Column(name = "correction_at")
    private Date correctionAt;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 課コード名. */
    @Column(name = "delivery_location")
    private String deliveryLocation;

    /** FAX. */
    @Column(name = "fax")
    private String fax;

    /** 会社名. */
    @Column(name = "company_name")
    private String companyName;

    /** 郵便番号. */
    @Column(name = "postal_code")
    private String postalCode;

    /** 住所. */
    @Column(name = "address")
    private String address;

    /** 電話番号. */
    @Column(name = "tel")
    private String tel;

}
