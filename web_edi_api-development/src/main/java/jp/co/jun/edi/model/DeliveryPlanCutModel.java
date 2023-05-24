package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品予定裁断のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryPlanCutModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品予定ID. */
    private BigInteger deliveryPlanId;

    /** 色. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 裁断数. */
    private int deliveryPlanCutLot;
}
