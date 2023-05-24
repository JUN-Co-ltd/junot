package jp.co.jun.edi.entity;

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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.AllocationTypeConverter;
import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.CarryTypeConverter;
import jp.co.jun.edi.entity.converter.LinkingStatusConverter;
import jp.co.jun.edi.entity.converter.SaleTypeConverter;
import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.SaleType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品明細情報のEntity.
 */
@Entity
@Table(name = "t_delivery_detail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryDetailEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品No. */
    @Column(name = "delivery_number")
    private String deliveryNumber;

    /** 納品依頼日. */
    @Column(name = "delivery_request_at")
    private Date deliveryRequestAt;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 店舗別登録済フラグ. */
    @Column(name = "store_registered_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType storeRegisteredFlg;

    /** 配分区分. */
    @Column(name = "allocation_type")
    @Convert(converter = AllocationTypeConverter.class)
    private AllocationType allocationType;

    /** キャリー区分. */
    @Column(name = "carry_type")
    @Convert(converter = CarryTypeConverter.class)
    private CarryType carryType;

    /** セール対象品区分. */
    @Column(name = "sale_type")
    @Convert(converter = SaleTypeConverter.class)
    private SaleType saleType;

    /** 納品依頼No. */
    @Column(name = "delivery_request_number")
    private String deliveryRequestNumber;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 納期. */
    @Column(name = "delivery_at")
    private Date deliveryAt;

    /** 修正納期. */
    @Column(name = "correction_at")
    private Date correctionAt;

    /** 配分出荷日. */
    @Column(name = "allocation_cargo_at")
    private Date allocationCargoAt;

    /** 配分完納フラグ. */
    @Column(name = "allocation_complete_payment_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType allocationCompletePaymentFlg;

    /** 配分確定フラグ. */
    @Column(name = "allocation_confirm_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType allocationConfirmFlg;

    /** 入荷フラグ. */
    @Column(name = "arrival_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType arrivalFlg;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 入荷管理No. */
    @Column(name = "arrival_number")
    private String arrivalNumber;

    /** 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    /** ピッキングフラグ. */
    @Column(name = "picking_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType pickingFlg;

    /** ピッキング日. */
    @Column(name = "picking_at")
    private Date pickingAt;

    /** 配分完了フラグ. */
    @Column(name = "allocation_complete_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType allocationCompleteFlg;

    /** 配分完了日. */
    @Column(name = "allocation_complete_at")
    private Date allocationCompleteAt;

    /** 配分計上日. */
    @Column(name = "allocation_record_at")
    private Date allocationRecordAt;

    /** 一括仕入用納品No. */
    @Column(name = "bulk_delivery_number")
    private String bulkDeliveryNumber;

    /** 出荷指示済フラグ. */
    @Column(name = "shipping_instructions_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType shippingInstructionsFlg;

    /** 出荷指示日. */
    @Column(name = "shipping_instructions_at")
    private Date shippingInstructionsAt;

    /** 出荷停止区分. */
    @Column(name = "shipping_stoped")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType shippingStoped;

    /** ファックス送信フラグ. */
    @Column(name = "fax_send")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType faxSend;

    /** 納品依頼書発行フラグ. */
    @Column(name = "delivery_sheet_out")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType deliverySheetOut;

    /** 配分率ID. */
    @Column(name = "distribution_ratio_id")
    private BigInteger distributionRatioId;

    /** 配分率. */
    @Column(name = "distribution_ratio")
    private BigDecimal distributionRatio = BigDecimal.ZERO;

    /** 配分方法. */
    @Column(name = "distribution_method")
    private String distributionMethod;

    /** 送信区分. */
    @Column(name = "send_code")
    private String sendCode;

    /** 送信日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "send_at")
    private Date sendAt;

    /** 送信回数. */
    @Column(name = "send_count")
    private int sendCount = 0;

    /** 連携入力者. */
    @Column(name = "junpc_tanto")
    private String junpcTanto;

    /** 連携ステータス. */
    @Column(name = "linking_status")
    @Convert(converter = LinkingStatusConverter.class)
    private LinkingStatusType linkingStatus;

    /** 連携日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "linked_at")
    private Date linkedAt;
}
