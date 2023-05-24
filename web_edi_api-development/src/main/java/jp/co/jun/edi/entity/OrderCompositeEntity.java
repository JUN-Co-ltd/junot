package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import jp.co.jun.edi.entity.converter.AutoTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteOrderTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteTypeConverter;
import jp.co.jun.edi.entity.converter.ExpenseItemTypeConverter;
import jp.co.jun.edi.entity.converter.LinkingStatusConverter;
import jp.co.jun.edi.entity.converter.OrderSheetOutTypeConverter;
import jp.co.jun.edi.type.AutoType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.OrderSheetOutType;
import lombok.Data;

/**
 * 発注一覧検索のEntity.
 */
@Entity
@Data
public class OrderCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 費目. */
    @Convert(converter = ExpenseItemTypeConverter.class)
    @Column(name = "expense_item")
    private ExpenseItemType expenseItem;

    /** 裁断自動区分. */
    @Convert(converter = AutoTypeConverter.class)
    @Column(name = "cut_auto_type")
    private AutoType cutAutoType;

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

    /** 生地m数. */
    @Column(name = "matl_meter")
    private BigDecimal matlMeter;

    /** 生地単価. */
    @Column(name = "matl_unit_price")
    private BigDecimal matlUnitPrice;

    /** 反番. */
    @Column(name = "cloth_number")
    private String clothNumber;

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

    /** 製品完納区分. */
    @Convert(converter = CompleteOrderTypeConverter.class)
    @Column(name = "product_complete_order")
    private CompleteOrderType productCompleteOrder;

    /** 製品済区分. */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "product_complete_type")
    private CompleteType productCompleteType;

    /** 全済区分. */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "all_completion_type")
    private CompleteType allCompletionType;

    /** 数量. */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

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

    /** 原産国コード. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 摘要. */
    @Column(name = "application")
    private String application;

    /** 送信区分. */
    @Column(name = "send_code")
    private String sendCode;

    /** 発注承認ステータス. */
    @Column(name = "order_approve_status")
    private String orderApproveStatus;

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

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 年度. */
    private Integer year;

    /** サブシーズン. */
    @Column(name = "sub_season_code")
    private String subSeasonCode;

    /** ブランド. */
    @Column(name = "brand_code")
    private String brandCode;

    /** ブランドソートコード. */
    @Column(name = "brand_sort_code")
    private String brandSortCode;

    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;

    /** 部門コード. */
    @Column(name = "dept_code")
    private String deptCode;

    /** 希望納品日. */
    @Column(name = "preferred_delivery_date")
    private String preferredDeliveryDate;

    /** プランナーコード. */
    @Column(name = "planner_code")
    private String plannerCode;

    /** パタンナーコード. */
    @Column(name = "pataner_code")
    private String patanerCode;

    /** 優良誤認区分. */
    @Column(name = "misleading_representation")
    private String misleadingRepresentation;

    /** 優良誤認承認区分（組成）. */
    @Column(name = "quality_composition_status")
    private String qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    @Column(name = "quality_coo_status")
    private String qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    @Column(name = "quality_harmful_status")
    private String qualityHarmfulStatus;

    /** 登録ステータス. */
    @Column(name = "regist_status")
    private int registStatus;

    /** 生産メーカー名. */
    @Column(name = "mdf_maker_name")
    private String mdfMakerName;

    /** 納品予定ID. */
    @Column(name = "delivery_plan_id")
    private BigInteger deliveryPlanId;

    /** 納品予定明細件数. */
    @Column(name = "delivery_plan_details_cnt")
    private int deliveryPlanDetailsCnt;

    /** 生産ステータス件数. */
    @Column(name = "production_status_cnt")
    private int productionStatusCnt;

    /** フクキタル資材発注確定1件以上存在する(true:利用可能、false:利用不可能). */
    @Column(name = "exists_material_order_confirm")
    private boolean existsMaterialOrderConfirm;
}
