package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.SaleType;
import lombok.Data;

/**
 * 納品明細情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryDetailModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** 納品No. */
    private String deliveryNumber;

    /** 納品依頼日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryRequestAt;

    /** 納品依頼回数. */
    private int deliveryCount;

    /** 課コード. */
    private String divisionCode;

    /** 店舗別登録済フラグ. */
    private BooleanType storeRegisteredFlg;

    /** 配分区分. */
    private String allocationType;

    /** キャリータイプ2種類あり. */
    private BooleanType hasBothCarryType;

    /** キャリー区分. */
    private CarryType carryType;

    /** セール対象品区分. */
    private SaleType saleType;

    /** 納品依頼No. */
    private String deliveryRequestNumber;

    /** 物流コード. */
    private String logisticsCode;

    /** 納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAt;

    /** 修正納期. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date correctionAt;

    /** 配分出荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationCargoAt;

    /**
     * 配分完納フラグ.
     * false：未完納(0)、true:完納(1)
     */
    private BooleanType allocationCompletePaymentFlg;

    /**
     * 配分確定フラグ.
     * false：未(0)、true:得意先配分済(1)
     */
    private BooleanType allocationConfirmFlg;

    /**
     * 入荷フラグ.
     * false：未(0)、true:製品仕入済(1)
     */
    private BooleanType arrivalFlg;

    /** 入荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date arrivalAt;

    /** 入荷管理No. */
    private String arrivalNumber;

    /** 入荷場所. */
    private String arrivalPlace;

    /**
     * ピッキングフラグ.
     * false：未(0)、true:ピッキングリスト出力済(1)
     */
    private BooleanType pickingFlg;

    /** ピッキング日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date pickingAt;

    /**
     * 配分完了フラグ.
     * false：未(0)、true:配分完了(1)
     */
    private BooleanType allocationCompleteFlg;

    /** 配分完了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationCompleteAt;

    /** 配分計上日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationRecordAt;

    /** 一括仕入用納品No. */
    private String bulkDeliveryNumber;

    /**
     * 出荷指示済フラグ.
     * false：未(0)、true:配分出荷伝票作成により(1)
     */
    private BooleanType shippingInstructionsFlg;

    /** 出荷指示日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date shippingInstructionsAt;

    /**
     * 出荷停止区分.
     * false：通常(0)、true:停止(1)
     */
    private BooleanType shippingStoped;

    /**
     * ファックス送信フラグ.
     * false：FAX無し(0)、true:FAX有り(1)
     */
    private BooleanType faxSend;

    /**
     * 納品依頼書発行フラグ.
     * false：未発行(0)、true:発行済(1)
     */
    private BooleanType deliverySheetOut;

    /** 配分率ID. */
    private BigInteger distributionRatioId;

    /** 連携入力者. */
    private String junpcTanto;

    /** 連携ステータス. */
    private LinkingStatusType linkingStatus;

    /** 連携日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date linkedAt;

    /** 場所コード. */
    private String allocationCode;

    /** 納品依頼ファイル情報. */
    private DeliveryFileInfoModel deliveryFileInfo;

    /** 納品SKU情報のリスト. */
    private List<DeliverySkuModel> deliverySkus;

    /** 納品得意先情報のリスト. */
    private List<DeliveryStoreModel> deliveryStores;
}
