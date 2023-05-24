package jp.co.jun.edi.entity.extended;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.LinkingStatusConverter;
import jp.co.jun.edi.entity.converter.PsTypeConverter;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.PsType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張品番情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTItemEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 希望納品日. */
    @Column(name = "preferred_delivery_date")
    private Date preferredDeliveryDate;

    /** 納品日. */
    @Column(name = "delivery_date")
    private Date deliveryDate;

    /** 仮発注日. */
    @Column(name = "provi_order_date")
    private Date proviOrderDate;

    /** 投入日. */
    @Column(name = "deployment_date")
    private Date deploymentDate;

    /** 投入週. */
    @Column(name = "deployment_week")
    private Integer deploymentWeek;

    /** P終了日. */
    @Column(name = "p_end_date")
    private Date pendDate;

    /** P終了週. */
    @Column(name = "p_end_week")
    private Integer pendWeek;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 品名カナ. */
    @Column(name = "product_name_kana")
    private String productNameKana;

    /** 年度. */
    @Column(name = "year")
    private Integer year;

    /** シーズン. */
    @Column(name = "season_code")
    private String seasonCode;

    /** サブシーズン. */
    @Column(name = "sub_season_code")
    private String subSeasonCode;

    /** 発注先メーカーID(最新生地). */
    @Column(name = "current_matl_order_supplier_id")
    private BigInteger currentMatlOrderSupplierId;

    /** 発注先メーカーID(最新製品). */
    @Column(name = "current_product_order_supplier_id")
    private BigInteger currentProductOrderSupplierId;

    /** 原産国. */
    @Column(name = "coo_code")
    private String cooCode;

    /** PS区分. */
    @Column(name = "ps_type")
    @Convert(converter = PsTypeConverter.class)
    private PsType psType;

    /** 原産国名. */
    @Column(name = "coo_name")
    private String cooName;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** セール上代. */
    @Column(name = "sale_retail_price")
    private BigDecimal saleRetailPrice;

    /** 生地原価. */
    @Column(name = "matl_cost")
    private BigDecimal matlCost;

    /** 加工賃. */
    @Column(name = "processing_cost")
    private BigDecimal processingCost;

    /** 付属品. */
    @Column(name = "accessories_cost")
    private BigDecimal accessoriesCost;

    /** その他原価. */
    @Column(name = "other_cost")
    private BigDecimal otherCost;

    /** 企画担当. */
    @Column(name = "planner_code")
    private String plannerCode;

    /** 製造担当. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /** パターンナー. */
    @Column(name = "pataner_code")
    private String patanerCode;

    /** パターンNo. */
    @Column(name = "pattern_no")
    private String patternNo;

    /** 丸井デプトブランド. */
    @Column(name = "marui_dept_brand")
    private String maruiDeptBrand;

    /** 丸井品番. */
    @Column(name = "marui_garment_no")
    private String maruiGarmentNo;

    /** Voi区分. */
    @Column(name = "voi_code")
    private String voiCode;

    /** 素材. */
    @Column(name = "material_code")
    private String materialCode;

    /** ゾーン. */
    @Column(name = "zone_code")
    private String zoneCode;

    /** ブランド. */
    @Column(name = "brand_code")
    private String brandCode;

    /** サブブランド. */
    @Column(name = "sub_brand_code")
    private String subBrandCode;

    /** ブランドソート. */
    @Column(name = "brand_sort_code")
    private String brandSortCode;

    /** 部門. */
    @Column(name = "dept_code")
    private String deptCode;

    /** アイテム. */
    @Column(name = "item_code")
    private String itemCode;

    /** テイスト. */
    @Column(name = "taste_code")
    private String tasteCode;

    /** タイプ1. */
    @Column(name = "type1_code")
    private String type1Code;

    /** タイプ2. */
    @Column(name = "type2_code")
    private String type2Code;

    /** タイプ3. */
    @Column(name = "type3_code")
    private String type3Code;

    /** 福袋. */
    @Column(name = "grab_bag")
    private boolean grabBag;

    /** 在庫管理区分. */
    @Column(name = "inventory_management_type")
    private boolean inventoryManagementType;

    /** 評価減区分. */
    @Column(name = "devaluation_type")
    private boolean devaluationType;

    /** 軽減税率対象フラグ. */
    @Column(name = "reduced_tax_rate_flg")
    private boolean reducedTaxRateFlg;

    /** 消化委託区分. */
    @Column(name = "digestion_commission_type")
    private boolean digestionCommissionType;

    /** アウトレット区分. */
    @Column(name = "outlet_code")
    private String outletCode;

    /** アウトレット区分名. */
    @Column(name = "outlet_name")
    private String outletName;

    /** メーカー品番. */
    @Column(name = "maker_garment_no")
    private String makerGarmentNo;

    /** メモ. */
    @Column(name = "memo")
    private String memo;

    /** 商品管理メッセージフラグ. */
    @Column(name = "item_massage_display")
    private boolean itemMassageDisplay;

    /** 商品管理メッセージ. */
    @Column(name = "item_massage")
    private String itemMassage;

    /** 登録ステータス. */
    @Column(name = "regist_status")
    private int registStatus;

    /** サンプル. */
    @Column(name = "sample")
    private boolean sample;

    /** 優良誤認区分. */
    @Column(name = "misleading_representation")
    private boolean misleadingRepresentation;

    /** 優良誤認承認区分（組成）. */
    @Column(name = "quality_composition_status")
    private int qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    @Column(name = "quality_coo_status")
    private int qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    @Column(name = "quality_harmful_status")
    private int qualityHarmfulStatus;

    /** JAN区分. */
    @Column(name = "jan_type")
    private JanType janType;

    /** 外部連携区分. */
    @Column(name = "external_linking_type")
    private String externalLinkingType;

    /** 停止フラグ. */
    @Column(name = "stopped")
    private boolean stopped;

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

    /**
     * 企画担当名称.
     */
    @Column(name = "planner_name")
    private String plannerName;

    /**
     * 製造担当名称.
     */
    @Column(name = "mdf_staff_name")
    private String mdfStaffName;

    /**
     * パターンナー名称.
     */
    @Column(name = "pataner_name")
    private String patanerName;
}
