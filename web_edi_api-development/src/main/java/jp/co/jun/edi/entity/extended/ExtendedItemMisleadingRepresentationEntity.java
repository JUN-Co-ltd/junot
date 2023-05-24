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

import jp.co.jun.edi.entity.converter.MSirmstYugaikbnTypeConverter;
import jp.co.jun.edi.entity.converter.QualityApprovalTypeConverter;
import jp.co.jun.edi.type.MSirmstYugaikbnType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * 拡張優良誤認用品番情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedItemMisleadingRepresentationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 投入日. */
    @Column(name = "deployment_date")
    private Date deploymentDate;

    /** 投入週. */
    @Column(name = "deployment_week")
    private Integer deploymentWeek;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 年度. */
    @Column(name = "year")
    private Integer year;

    /** シーズン. */
    @Column(name = "season_code")
    private String seasonCode;

    /** 生産メーカー. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 生産メーカー名称. */
    @Column(name = "mdf_maker_name")
    private String mdfMakerName;

    /** 有害物質対応区分. */
    @Column(name = "hazardous_substance_response_type")
    @Convert(converter = MSirmstYugaikbnTypeConverter.class)
    private MSirmstYugaikbnType hazardousSubstanceResponseType;

    /** 有害物質対応日付. */
    @Column(name = "hazardous_substance_response_at")
    private String hazardousSubstanceResponseAt;

    /** 原産国. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 原産国名. */
    @Column(name = "coo_name")
    private String cooName;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

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

    /** 企画担当名称. */
    @Column(name = "planner_name")
    private String plannerName;

    /** 製造担当. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /** 製造担当名称. */
    @Column(name = "mdf_staff_name")
    private String mdfStaffName;

    /** パターンナー. */
    @Column(name = "pataner_code")
    private String patanerCode;

    /** パターンナー名称. */
    @Column(name = "pataner_name")
    private String patanerName;

    /** 素材. */
    @Column(name = "material_code")
    private String materialCode;

    /** 素材名. */
    @Column(name = "material_name")
    private String materialName;

    /** ブランド. */
    @Column(name = "brand_code")
    private String brandCode;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** アイテム. */
    @Column(name = "item_code")
    private String itemCode;

    /** アイテム名. */
    @Column(name = "item_name")
    private String itemName;

    /** メモ. */
    @Column(name = "memo")
    private String memo;

    /** 優良誤認承認区分（組成）. */
    @Column(name = "quality_composition_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    @Column(name = "quality_coo_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    @Column(name = "quality_harmful_status")
    @Convert(converter = QualityApprovalTypeConverter.class)
    private QualityApprovalType qualityHarmfulStatus;

    /** 発注番号. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 発注数. */
    @Column(name = "quantity")
    private BigDecimal quantity;
}
