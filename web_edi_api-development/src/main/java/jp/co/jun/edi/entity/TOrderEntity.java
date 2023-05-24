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

import jp.co.jun.edi.entity.converter.AutoTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteOrderTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteTypeConverter;
import jp.co.jun.edi.entity.converter.ExpenseItemTypeConverter;
import jp.co.jun.edi.entity.converter.LinkingStatusConverter;
import jp.co.jun.edi.entity.converter.OrderSheetOutTypeConverter;
import jp.co.jun.edi.entity.converter.ProductionTypeConverter;
import jp.co.jun.edi.entity.converter.ReissueTypeConverter;
import jp.co.jun.edi.entity.converter.SendTypeConverter;
import jp.co.jun.edi.type.AutoType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.OrderSheetOutType;
import jp.co.jun.edi.type.ProductionType;
import jp.co.jun.edi.type.ReissueType;
import jp.co.jun.edi.type.SendType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 発注情報のEntity.
 */
@Entity
@Table(name = "t_order")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TOrderEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 企画番号. */
    @Column(name = "plan_number")
    private String planNumber;

    /**
     * 発注No.
     */
    @Column(name = "order_number")
    private BigInteger orderNumber = BigInteger.ZERO;

    /** 関連No. */
    @Column(name = "relation_number")
    private BigInteger relationNumber;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** MD仮品番. */
    @Column(name = "md_provisional_part_no")
    private String mdProvisionalPartNo;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 生産区分. */
    @Convert(converter = ProductionTypeConverter.class)
    @Column(name = "production_type")
    private ProductionType productionType;

    /** 素材. */
    @Column(name = "material")
    private String material;

    /** 費目. */
    @Convert(converter = ExpenseItemTypeConverter.class)
    @Column(name = "expense_item")
    private ExpenseItemType expenseItem;

    /** 生地発注日. */
    @Column(name = "matl_order_at")
    private Date matlOrderAt;

    /** 生地メーカー. */
    @Column(name = "matl_maker_code")
    private String matlMakerCode;

    /** 生地品番. */
    @Column(name = "matl_part_no")
    private String matlPartNo;

    /** 生地品名. */
    @Column(name = "matl_product_name")
    private String matlProductName;

    /** 生地納期. */
    @Column(name = "matl_delivery_at")
    private Date matlDeliveryAt;

    /** 生地修正納期. */
    @Column(name = "matl_correction_delivery_at")
    private Date matlCorrectionDeliveryAt;

    /** 生地仕入区分. */
    @Column(name = "matl_purchase_type")
    private String matlPurchaseType;

    /** 生地m数. */
    @Column(name = "matl_meter")
    private BigDecimal matlMeter;

    /** 生地単価. */
    @Column(name = "matl_unit_price")
    private BigDecimal matlUnitPrice;

    /** 反番. */
    @Column(name = "cloth_number")
    private String clothNumber;

    /** 反数. */
    @Column(name = "cloth_count")
    private BigDecimal clothCount;

    /** 予定用尺. */
    @Column(name = "plan_length_actual")
    private BigDecimal planLengthActual;

    /** 実用尺. */
    @Column(name = "necessary_length_actual")
    private BigDecimal necessaryLengthActual;

    /** 用尺単位. */
    @Column(name = "necessary_length_unit")
    private String necessaryLengthUnit;

    /** 生地原価. */
    @Column(name = "matl_cost")
    private BigDecimal matlCost;

    /** 生産メーカー. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 生産工場. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 生産工場名. */
    @Column(name = "mdf_maker_factory_name")
    private String mdfMakerFactoryName;

    /** 委託先工場名. */
    @Column(name = "consignment_factory")
    private String consignmentFactory;

    /** 原産国. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 製品発注日. */
    @Column(name = "product_order_at")
    private Date productOrderAt;

    /** 製品納期. */
    @Column(name = "product_delivery_at")
    private Date productDeliveryAt;

    /** 製品修正納期. */
    @Column(name = "product_correction_delivery_at")
    private Date productCorrectionDeliveryAt;

    /** 発注完了日. */
    @Column(name = "order_complete_at")
    private Date orderCompleteAt;

    /** 製造担当コード. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /**
     * 裁断完納区分.
     */
    @Convert(converter = CompleteOrderTypeConverter.class)
    @Column(name = "cut_complete_order_type")
    private CompleteOrderType cutCompleteOrderType = CompleteOrderType.INCOMPLETE;

    /**
     * 製品完納区分.
     */
    @Convert(converter = CompleteOrderTypeConverter.class)
    @Column(name = "product_complete_order")
    private CompleteOrderType productCompleteOrder = CompleteOrderType.INCOMPLETE;

    /**
     * 生地完納区分.
     */
    @Convert(converter = CompleteOrderTypeConverter.class)
    @Column(name = "matl_complete_order_type")
    private CompleteOrderType matlCompleteOrderType = CompleteOrderType.INCOMPLETE;

    /**
     * 裁断済区分.
     */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "cut_complete_type")
    private CompleteType cutCompleteType = CompleteType.INCOMPLETE;

    /**
     * 製品済区分.
     */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "product_complete_type")
    private CompleteType productCompleteType = CompleteType.INCOMPLETE;

    /**
     * 生地済区分.
     */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "matl_complete_type")
    private CompleteType matlCompleteType = CompleteType.INCOMPLETE;

    /**
     * 全済区分.
     */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "all_completion_type")
    private CompleteType allCompletionType = CompleteType.INCOMPLETE;

    /**
     * 再発行フラグ.
     */
    @Convert(converter = ReissueTypeConverter.class)
    @Column(name = "reissue_flg")
    private ReissueType reissueFlg = ReissueType.NO_REISSUE;

    /** 反巾. */
    @Column(name = "cloth_width")
    private int clothWidth;

    /** 反長. */
    @Column(name = "cloth_length")
    private int clothLength;

    /** 数量. */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 製品金額. */
    @Column(name = "product_price")
    private BigDecimal productPrice;

    /** 製品原価. */
    @Column(name = "product_cost")
    private BigDecimal productCost;

    /** 加工賃. */
    @Column(name = "processing_cost")
    private BigDecimal processingCost;

    /** 附属代. */
    @Column(name = "attached_cost")
    private BigDecimal attachedCost;

    /** その他原価. */
    @Column(name = "other_cost")
    private BigDecimal otherCost;

    /** B級品単価. */
    @Column(name = "non_conforming_product_unit_price")
    private BigDecimal nonConformingProductUnitPrice;

    /** 輸入区分. */
    @Column(name = "import_code")
    private String importCode;

    /**
     * サイズ区分.
     */
    @Column(name = "size_type")
    private String sizeType = "00";

    /** 摘要. */
    @Column(name = "application")
    private String application;

    /**
     * 裁断回数.
     */
    @Column(name = "cut_count")
    private int cutCount = 0;

    /**
     * 納品依頼回数.
     */
    @Column(name = "delivery_count")
    private int deliveryCount = 0;

    /**
     * 発注承認ステータス.
     */
    @Column(name = "order_approve_status")
    private String orderApproveStatus = "0";

    /** 発注確定日. */
    @Column(name = "order_confirm_at")
    private Date orderConfirmAt;

    /** 発注確定者. */
    @Column(name = "order_confirm_user_id")
    private BigInteger orderConfirmUserId;

    /** 発注承認日. */
    @Column(name = "order_approve_at")
    private Date orderApproveAt;

    /** 発注承認者. */
    @Column(name = "order_approve_user_id")
    private BigInteger orderApproveUserId;

    /** 発注書出力フラグ. */
    @Convert(converter = OrderSheetOutTypeConverter.class)
    @Column(name = "order_sheet_out")
    private OrderSheetOutType orderSheetOut;

    /** 生地最終処理日. */
    @Column(name = "matl_last_disposal_at")
    private Date matlLastDisposalAt;

    /** 製品最終処理日. */
    @Column(name = "product_last_disposal_at")
    private Date productLastDisposalAt;

    /** 最新裁断日. */
    @Column(name = "current_cut_at")
    private Date currentCutAt;

    /** 最新生地入荷日. */
    @Column(name = "current_matl_arrival_at")
    private Date currentMatlArrivalAt;

    /** 最新納品依頼日. */
    @Column(name = "current_delivery_request_at")
    private Date currentDeliveryRequestAt;

    /** 最新製品仕入日. */
    @Column(name = "current_product_purchase_at")
    private Date currentProductPurchaseAt;

    /** 最新製品返品日. */
    @Column(name = "current_product_return_at")
    private Date currentProductReturnAt;

    /** 裁断自動区分. */
    @Convert(converter = AutoTypeConverter.class)
    @Column(name = "cut_auto_type")
    private AutoType cutAutoType;

    /** 発注承認自動. */
    @Convert(converter = AutoTypeConverter.class)
    @Column(name = "order_auto_approve_flg")
    private AutoType orderAutoApproveFlg;

    /** 配分自動. */
    @Convert(converter = AutoTypeConverter.class)
    @Column(name = "distribute_auto_flg")
    private AutoType distributeAutoFlg;

    /** SQ送信区分. */
    @Convert(converter = SendTypeConverter.class)
    @Column(name = "sq_send_type")
    private SendType sqSendType;

    /** 送信区分. */
    @Column(name = "send_code")
    private String sendCode;

    /** 送信日. */
    @Column(name = "send_at")
    private Date sendAt;

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

    // PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ. */
    @Column(name = "tagdat_created_flg")
    private String tagdatCreatedFlg;

    /** TAGDAT作成日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "tagdat_created_at")
    private Date tagdatCreatedAt;
    // PRD_0142 #10423 JFE add end
}
