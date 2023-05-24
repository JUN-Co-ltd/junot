package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入確定(LG送信)用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseConfirmModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** 課コード. */
    private String divisionCode;

    /** 引取回数(納品明細の納品回数). */
    private Integer purchaseCount;
}
