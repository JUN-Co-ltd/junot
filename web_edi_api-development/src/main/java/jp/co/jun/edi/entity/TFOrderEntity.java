package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterConfirmStatusTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterDeliveryTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterLinkingStatusTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterOrderTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterConfirmStatusType;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterLinkingStatusType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the t_f_order database table.
 *
 */
@Entity
@Table(name = "t_f_order")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TFOrderEntity extends GenericEntity {
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
    @Column(name = "order_id", updatable = false)
    private BigInteger orderId;

    /** 請求先ID. */
    @Column(name = "billing_company_id")
    private BigInteger billingCompanyId;

    /** 契約No. */
    @Column(name = "contract_number")
    private String contractNumber;

    /** 納入先ID. */
    @Column(name = "delivery_company_id")
    private BigInteger deliveryCompanyId;

    /** 納入先担当者. */
    @Column(name = "delivery_staff")
    private String deliveryStaff;

    /** 工場No. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 発注日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "order_at")
    private Date orderAt;

    /** オーダー識別コード. */
    @Column(name = "order_code")
    private String orderCode;

    /** 発注者ユーザID. */
    @Column(name = "order_user_id", updatable = false)
    private BigInteger orderUserId;

    /** 希望出荷日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "preferred_shipping_at")
    private Date preferredShippingAt;

    /** リピート数. */
    @Column(name = "repeat_number")
    private Integer repeatNumber;

    /** 特記事項. */
    @Column(name = "special_report")
    private String specialReport;

    /** 備考. */
    @Column(name = "remarks")
    private String remarks;

    /**
     * 緊急.
     * false:緊急ではない(0)、true:緊急(1)
     */
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType urgent;

    /** 手配先. */
    @Convert(converter = FukukitaruMasterDeliveryTypeConverter.class)
    @Column(name = "delivery_type")
    private FukukitaruMasterDeliveryType deliveryType;

    /** 確定ステータス. */
    @Convert(converter = FukukitaruMasterConfirmStatusTypeConverter.class)
    @Column(name = "confirm_status")
    private FukukitaruMasterConfirmStatusType confirmStatus;

    /** 発注メール送信日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "order_send_at")
    private Date orderSendAt;

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

   /** 連携ステータス. */
    @Convert(converter = FukukitaruMasterLinkingStatusTypeConverter.class)
    @Column(name = "linking_status")
    private FukukitaruMasterLinkingStatusType linkingStatus;


}
