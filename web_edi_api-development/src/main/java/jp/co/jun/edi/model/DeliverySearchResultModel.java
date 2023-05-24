package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * 配分一覧(納品情報)の検索結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverySearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品依頼ID. */
    private BigInteger deliveryId;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 承認ステータス. */
    private String deliveryApproveStatus;

    /** 納品依頼回数. */
    private int deliveryCount;

    /** 納品日(修正納期). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date correctionAt;

    /** 店舗別登録済フラグ. */
    private BooleanType storeRegisteredFlg;

    // PRD_0087 mod SIT start
    ///** 配分確定フラグ. */
    //private BooleanType allocationConfirmFlg;
    /** 配分完了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationCompleteAt;

    /** 配分計上日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationRecordAt;
    // PRD_0087 mod SIT end

    /** 発注数量. */
    private BigDecimal quantity;

    /**
     * 完納フラグ.
     * false：未完(0)、true:完納(1)
     * */
    private boolean orderCompleteFlg;

    /** 取引数(納品SKUの納品数量の合計). */
    private int transactionLot;

    /** 配分数(納品得意先SKUの納品数量の合計). */
    private int allocationLot;

    //PRD_0127 #9837 add JFE start
    /** 納品先. */
    private String companyName;
    //PRD_0127 #9837 add JFE end

    /** 仕入確定数. */
    private int fixArrivalCount;

    /** 入荷フラグ.*/
    private BooleanType arrivalFlg;

    /** 出荷指示フラグ.*/
    private BooleanType shippingInstructionsFlg;

}
