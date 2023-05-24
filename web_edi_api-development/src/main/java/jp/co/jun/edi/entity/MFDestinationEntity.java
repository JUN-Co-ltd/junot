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
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル用宛先会社情報.
 *
 */
@Entity
@Table(name = "m_f_destination")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFDestinationEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** フクキタル連携用の会社コード. */
    @Column(name = "fukukitaru_company_code")
    private String fukukitaruCompanyCode;

    /** 住所. */
    private String address;

    /** 会社名. */
    @Column(name = "company_name")
    private String companyName;

    /** FAX番号. */
    private String fax;

    /** 郵便番号. */
    @Column(name = "postal_code")
    private String postalCode;

    /** 電話番号. */
    private String tel;

    /**
     * 承認需要フラグ.
     * false:承認不要(0)、true:承認必要 (1)
     */
    @Convert(converter = BooleanTypeConverter.class)
    @Column(name = "is_approval_required")
    private BooleanType isApprovalRequired;
}
