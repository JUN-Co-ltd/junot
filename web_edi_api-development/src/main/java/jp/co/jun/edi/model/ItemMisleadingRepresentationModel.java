package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MSirmstYugaikbnType;
import jp.co.jun.edi.type.QualityApprovalType;
import lombok.Data;

/**
 * 優良誤認情報Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemMisleadingRepresentationModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 最新発注ID. */
    private BigInteger latestOrderId;

    /** 最新発注No. */
    private BigInteger latestOrderNumber;

    /** 最新発注数. */
    private BigDecimal latestQuantity;

    /** 生産メーカー. */
    private String mdfMakerCode;

    /** 生産メーカー名称. */
    private String mdfMakerName;

    /** 有害物質対応区分. */
    private MSirmstYugaikbnType hazardousSubstanceResponseType;

    /** 有害物質対応日付. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date hazardousSubstanceResponseAt;

    /** 原産国コード. */
    private String cooCode;

    /** 原産国名称. */
    private String cooName;

    /** 上代. */
    private BigDecimal retailPrice;

    /** 生地原価. */
    private BigDecimal matlCost;

    /** 加工賃. */
    private BigDecimal processingCost;

    /** 付属品. */
    private BigDecimal accessoriesCost;

    /** その他原価. */
    private BigDecimal otherCost;

    /** 投入日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deploymentDate;

    /** 投入週. */
    private Integer deploymentWeek;

    /** ブランドコード. */
    private String brandCode;

    /** ブランド名. */
    private String brandName;

    /** アイテムコード. */
    private String itemCode;

    /** アイテム名. */
    private String itemName;

    /** 年度. */
    private Integer year;

    /** シーズンコード. */
    private String seasonCode;

    /** 優良誤認承認区分（組成）. */
    private QualityApprovalType qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private QualityApprovalType qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private QualityApprovalType qualityHarmfulStatus;

    /** 素材. */
    private String materialCode;

    /** 素材名. */
    private String materialName;

    /** 企画担当. */
    private String plannerCode;

    /** 企画担当名称. */
    private String plannerName;

    /** 製造担当. */
    private String mdfStaffCode;

    /** 製造担当名称. */
    private String mdfStaffName;

    /** パターンナー. */
    private String patanerCode;

    /** パターンナー名称. */
    private String patanerName;

    /** メモ. */
    private String memo;

    /** 発注番号. */
    private BigInteger orderNumber;

    /** 発注数. */
    private BigDecimal quantity;

    /** SKU情報のリスト. */
    private List<SkuModel> skus;

    /** 組成情報のリスト. */
    private List<CompositionModel> compositions;

    /** 品番ファイル情報(タンザク). */
    private ItemFileInfoModel tanzakuItemFileInfo;

    /** 優良誤認検査ファイル情報のリスト. */
    private List<MisleadingRepresentationFileModel> misleadingRepresentationFiles;

    /** 優良誤認承認情報のリスト. */
    private List<MisleadingRepresentationModel> misleadingRepresentations;

    /** 読み取り専用フラグ. */
    private boolean readOnly;
}
