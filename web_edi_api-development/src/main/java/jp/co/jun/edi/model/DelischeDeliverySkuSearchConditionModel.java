package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * デリスケ納品SKU情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelischeDeliverySkuSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    private BigInteger orderId;

    /** 納品ID. */
    private BigInteger deliveryId;
}
