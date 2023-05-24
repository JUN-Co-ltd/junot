package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * 納品情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 納品依頼回数. */
    private Integer deliveryCount;

    /** 最終納品ステータス. */
    private String lastDeliveryStatus;

    /** 承認ステータス. */
    private String deliveryApproveStatus;

    /** 承認日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryApproveAt;

    /** 配分率区分. */
    private String distributionRatioType;

    /** メモ. */
    private String memo;

    /** 納期変更理由ID. */
    private Integer deliveryDateChangeReasonId;

    /** 納品変更理由詳細. */
    private String deliveryDateChangeReasonDetail;

    /** B級品区分. */
    private boolean nonConformingProductType;

    /** B級品単価. */
    private BigDecimal nonConformingProductUnitPrice;

    /** 納品ID降順ソート. */
    private boolean idSortDesc;

    /** 納品明細情報のリスト. */
    private List<DeliveryDetailModel> deliveryDetails;

    /** SQロックユーザーID. */
    private BigInteger sqLockUserId;

    /** SQロックユーザーアカウント名. */
    private String sqLockUserAccountName;

    /** 店舗別画面登録フラグ. */
    private BooleanType fromStoreScreenFlg;

    /** 納品伝票ファイルのリスト. */
    private List<DeliveryVoucherFileInfoModel> deliveryVoucherFileInfos;

    // PRD_0044 add SIT start
    /** 店舗別画面一時保存フラグ. */
    private BooleanType storeScreenSaveCorrectFlg = BooleanType.FALSE;
    // PRD_0044 add SIT end
}
