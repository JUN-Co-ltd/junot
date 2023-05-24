package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品情報ViewのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VDeliveryModel implements Serializable {
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
    private int deliveryCount;

    /** 最終納品ステータス. */
    private String lastDeliveryStatus;

    /** 承認ステータス. */
    private String deliveryApproveStatus;

    /** 承認日. */
    private Date deliveryApproveAt;

    /** 納品依頼日. */
    private Date deliveryRequestAt;

    /** 修正納期. */
    private Date correctionAt;

    /** 納品依頼数合計. */
    private int sumDeliveryLot;
}
