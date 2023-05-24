package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.ItemValidationType;
import jp.co.jun.edi.type.JanType;
import jp.co.jun.edi.type.LinkingStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 品番情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ItemModel extends GenericModel implements Serializable {
//    private static final int YEAR_MIN = 2000;
//    private static final int YEAR_MAX = 2999;

    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番. */
//    @NotBlank(message = "partNo,required", groups = { Default.class })
//    @Pattern(regexp = "[A-Z]{3}[0-9]{4}[0]", message = "partNo,pattern", groups = { Default.class })
    private String partNo;

    /** 希望納品日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date preferredDeliveryDate;

    /** 納品日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryDate;

    /** 仮発注日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date proviOrderDate;

    /** 投入日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deploymentDate;

    /** 投入週. */
    private Integer deploymentWeek;

    /** P終了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date pendDate;

    /** P終了週. */
    private Integer pendWeek;

    /** 品名. */
//    @NotBlank(message = "productName,required", groups = { Default.class })
    private String productName;

    /** 品名カナ. */
//    @NotBlank(message = "productNameKana,required", groups = { Default.class })
    private String productNameKana;

    /** 年度. */
//    @NotNull(message = "year,required", groups = { Default.class })
//    @Min(value = YEAR_MIN, message = "year,pattern", groups = { Default.class })
//    @Max(value = YEAR_MAX, message = "year,pattern", groups = { Default.class })
    private Integer year;

    /** シーズン. */
//    @NotBlank(message = "seasonCode,required", groups = { Default.class })
    private String seasonCode;

    /** サブシーズン. */
//    @NotBlank(message = "subSeasonCode,required", groups = { Default.class })
    private String subSeasonCode;

    /** 生地メーカーコード. */
//    @Pattern(regexp = "[0-9]{5}", message = "matlMakerCode,pattern", groups = { Default.class })
    private String matlMakerCode;

    /** 生地メーカー名称. */
    private String matlMakerName;

    /** 生産メーカー. */
//    @NotBlank(message = "mdfMakerCode,required", groups = { Default.class })
//    @Pattern(regexp = "[0-9]{5}", message = "mdfMakerCode,pattern", groups = { Default.class })
    private String mdfMakerCode;

    /** 生産メーカー名称. */
    private String mdfMakerName;

    /** 発注先メーカーID(最新生地). */
    private BigInteger currentMatlOrderSupplierId;

    /** 発注先メーカーID(最新製品). */
    private BigInteger currentProductOrderSupplierId;

    /** 生産工場コード. */
//    @Pattern(regexp = "[0-9]{6}", message = "mdfMakerFactoryCode,pattern", groups = { Default.class })
    private String mdfMakerFactoryCode;

    /** 生産工場名. */
    private String mdfMakerFactoryName;

    /** 委託先工場. */
    private String consignmentFactory;

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

    /** 企画担当コード. */
//    @NotBlank(message = "plannerCode,required", groups = { Default.class })
//    @Pattern(regexp = "[0-9]{6}", message = "plannerCode,pattern", groups = { Default.class })
    private String plannerCode;

    /** 企画担当名称. */
    private String plannerName;

    /** 製造担当コード. */
//    @NotBlank(message = "mdfStaffCode,required", groups = { Default.class })
//    @Pattern(regexp = "[0-9]{6}", message = "mdfStaffCode,pattern", groups = { Default.class })
    private String mdfStaffCode;

    /** 製造担当名称. */
    private String mdfStaffName;

    /** パターンナーコード. */
//    @Pattern(regexp = "[0-9]{6}", message = "patanerCode,pattern", groups = { Default.class })
    private String patanerCode;

    /** 生産メーカー担当. */
    private BigInteger mdfMakerStaffId;

    /** パターンナー名称. */
    private String patanerName;

    /** パターンNo. */
//    @Pattern(regexp = "[a-zA-Z0-9-?]*", message = "patternNo,pattern", groups = { Default.class })
    private String patternNo;

    /** 丸井デプトブランド. */
    private String maruiDeptBrand;

    /** 丸井品番. */
    private String maruiGarmentNo;

    /** Voi区分. */
    private String voiCode;

    /** 素材. */
    private String materialCode;

    /** ゾーン. */
    private String zoneCode;

    /** ブランド. */
//    @NotBlank(message = "brandCode,required", groups = { Default.class })
//    @Pattern(regexp = "[A-Z]{2}", message = "brandCode,pattern", groups = { Default.class })
    private String brandCode;

    /** サブブランド. */
    private String subBrandCode;

    /** ブランドソート. */
    private String brandSortCode;

    /** 部門. */
    private String deptCode;

    /** アイテム. */
//    @NotBlank(message = "itemCode,required", groups = { Default.class })
//    @Pattern(regexp = "[A-Z]", message = "itemCode,pattern", groups = { Default.class })
    private String itemCode;

    /** テイスト. */
    private String tasteCode;

    /** タイプ1. */
    private String type1Code;

    /** タイプ2. */
    private String type2Code;

    /** タイプ3. */
    private String type3Code;

    /** 福袋. */
    private boolean grabBag;

    /** 在庫管理区分. */
    private boolean inventoryManagementType;

    /** 評価減区分. */
    private boolean devaluationType;

    /** 軽減税率対象フラグ. */
    private boolean reducedTaxRateFlg;

    /** 消化委託区分. */
    private boolean digestionCommissionType;

    /** アウトレット区分. */
    private String outletCode;

    /** アウトレット区分名. */
    private String outletName;

    /** メーカー品番. */
//    @Pattern(regexp = "[a-zA-Z0-9]*", message = "makerGarmentNo,pattern", groups = { Default.class })
    private String makerGarmentNo;

    /** メモ. */
    private String memo;

    /** 商品管理メッセージフラグ. */
    private boolean itemMassageDisplay;

    /** 商品管理メッセージ. */
    private String itemMassage;

    /** 登録ステータス. */
    private int registStatus;

    /** サンプル. */
    private boolean sample;

    /** 優良誤認区分. */
    private boolean misleadingRepresentation;

    /** 優良誤認承認区分（組成）. */
    private int qualityCompositionStatus;

    /** 優良誤認承認区分（国）. */
    private int qualityCooStatus;

    /** 優良誤認承認区分（有害物質）. */
    private int qualityHarmfulStatus;

    /** JAN区分. */
    @NotNull(groups = { Default.class })
    private JanType janType;

    /** 外部連携区分. */
    private String externalLinkingType;

    /** 停止フラグ. */
    private boolean stopped;

    /** 連携入力者. */
    private String junpcTanto;

    /** 連携ステータス. */
    @Value("${value:0}")
    private LinkingStatusType linkingStatus;

    /** 連携日時. */
    private Date linkedAt;

    /** 発注先メーカー情報のリスト. */
    private List<OrderSupplierModel> orderSuppliers;

    /** SKU情報のリスト. */
    private List<SkuModel> skus;

    /** 組成情報のリスト. */
    @Valid
    private List<CompositionModel> compositions;

    /** 品番ファイル情報のリスト. */
    private List<ItemFileInfoModel> itemFileInfos;

    /** 優良誤認検査ファイル情報のリスト. */
    private List<MisleadingRepresentationFileModel> misleadingRepresentationFiles;

    /** 発注情報のリスト. */
    private List<OrderModel> orders;

    /** フクキタル品番情報. */
    private FukukitaruItemModel fkItem;

    /**
     * 登録ステータス変更区分.
     * 画面上で「品番として登録」を押されての更新かどうか判断する.
     */
    private Integer changeRegistStatusType;

    /**
     * バリデーション区分.
     * (商品更新でも品番のバリデーションが必要.)
     */
    private ItemValidationType validationType;

    /** 読み取り専用. */
    private boolean readOnly;

    /** 受注・発注登録済み. */
    private boolean registeredOrder;

    /** 発注承認済み. */
    private boolean approvedOrder;

    /** 全ての発注が完納. */
    private boolean completedAllOrder;

    /** 納品依頼承認済み. */
    private boolean approvedDelivery;

    /** 優良誤認承認済み. */
    private boolean approvedMisleadingRepresentation;

    /** 優良誤認（組成）承認済みのカラーのリスト. */
    private List<String> approvedColors;
}
