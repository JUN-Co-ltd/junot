package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jp.co.jun.edi.entity.converter.MSirmstYugaikbnTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.type.MSirmstYugaikbnType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * 拡張優良誤認検査承認一覧情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedItemMisleadingRepresentationSearchResultEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 発注数. */
    private BigInteger quantity;

    /** 製造担当者コード. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /** 原産国コード. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 優良誤認承認区分（国）. */
    @Column(name = "quality_coo_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCooStatus;

    /** 組成コード. */
    @Column(name = "composition_code")
    private String compositionCode;

    /** 優良誤認承認区分（組成）. */
    @Column(name = "quality_composition_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCompositionStatus;

    /** 有害区分. */
    @Convert(converter = MSirmstYugaikbnTypeConverter.class)
    private MSirmstYugaikbnType yugaikbn;

    /** 優良誤認承認区分（有害物質）. */
    @Column(name = "quality_harmful_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityHarmfulStatus;

    /** 検査承認日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "Asia/Tokyo")
    @Column(name = "approval_at")
    private Date approvalAt;

}
