package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliverySearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String partNo;
    private BigInteger orderId;
    private BigInteger orderNumber;
    private BigInteger deliveryId;
    private boolean idSortDesc;
}
