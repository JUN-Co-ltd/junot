package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterOrderTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * フクキタル連携発注情報Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTFOrderLinkingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** フクキタル品番ID. */
    @Column(name = "f_item_id", updatable = false)
    private BigInteger fItemId;

    /** 品番ID. */
    @Column(name = "part_no_id", updatable = false)
    private BigInteger partNoId;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** オーダー識別コード. */
    @Column(name = "order_code")
    private String orderCode;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** ブランド名記号. */
    @Column(name = "brand_code")
    private String brandCode;

    /** 発注日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "order_at")
    private Date orderAt;

    /** 発注者ユーザID. */
    @Column(name = "order_user_id", updatable = false)
    private String orderUserId;

    /** 請求先ID. */
    @Column(name = "billing_company_id")
    private BigInteger billingCompanyId;

    /** 納入先ID. */
    @Column(name = "delivery_company_id")
    private BigInteger deliveryCompanyId;

    /** 納入先担当者. */
    @Column(name = "delivery_staff")
    private String deliveryStaff;

    /** 緊急. */
    @Column(name = "urgent")
    private String urgent;

    /** 希望出荷日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "preferred_shipping_at")
    private Date preferredShippingAt;

    /** 契約No. */
    @Column(name = "contract_number")
    private String contractNumber;

    /** 特記事項. */
    @Column(name = "special_report")
    private String specialReport;

    /** 手配先. */
    @Column(name = "delivery_type")
    private String deliveryType;

    /** リピート数. */
    @Column(name = "repeat_number")
    private String repeatNumber;

    /** 工場No. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 製品品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 発注種別. */
    @Convert(converter = FukukitaruMasterOrderTypeConverter.class)
    @Column(name = "order_type")
    private FukukitaruMasterOrderType orderType;

    /**
     * 責任発注.
     * false:メーカー責任発注ではない(0)、true:メーカー責任発注(1)
     */
    @Convert(converter = BooleanTypeConverter.class)
    @Column(name = "is_responsible_order")
    private BooleanType isResponsibleOrder;

    /** 優良誤認承認区分（組成）. */
    @Convert(converter = QualityApprovalTypeConverter.class)
    @Column(name = "quality_composition_status")
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    @Convert(converter = QualityApprovalTypeConverter.class)
    @Column(name = "quality_coo_status")
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    @Convert(converter = QualityApprovalTypeConverter.class)
    @Column(name = "quality_harmful_status")
    private QualityApprovalType qualityHarmfulStatus;
}
