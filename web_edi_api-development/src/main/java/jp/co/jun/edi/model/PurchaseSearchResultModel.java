package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * 仕入一覧画面用の検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** 納品No. */
    private String deliveryNumber;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 回数. */
    private int deliveryCount;

    /** 課コード. */
    private String divisionCode;

    /** キャリー区分. */
    private CarryType carryType;

    /** 仕入状態(仕入ステータス). */
    private BooleanType arrivalFlg;

    /** 納品日. */
    private Date correctionAt;

    /** 仕入先コード. */
    private String mdfMakerCode;

    /** 仕入先名. */
    private String mdfMakerName;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 配分数. */
    private int deliveryLot;

    /** 仕入(入荷)数合計(納品明細基準). */
    private int arrivalCountSum;

    /** 仕入(入荷)確定数合計(納品明細基準). */
    private int fixArrivalCountSum;

    /** 仕入登録済数. */
    private int purchaseRegisteredCount;

    /** 仕入指示送信済数. */
    private int purchaseConfirmedCount;

    /** LG送信区分. */
    private LgSendType lgSendType;
}
