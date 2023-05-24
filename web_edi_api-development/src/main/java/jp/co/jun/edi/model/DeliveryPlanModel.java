package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品予定のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryPlanModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 発注ID. */
    private BigInteger orderId;

    /** 品番ID. */
    private BigInteger partNoId;

    // PRD_0145 #10776 add JFE start
    /** 実用尺. */
    private BigDecimal necessaryLengthActual;
    // PRD_0145 #10776 add JFE end

    /** 登録済ステータス. */
    private int entryStatus;

    /** メモ. */
    private String memo;

    /** 納品予定明細のリスト. */
    private List<DeliveryPlanDetailModel> deliveryPlanDetails;

    /** 納品予定裁断のリスト. */
    private List<DeliveryPlanCutModel> deliveryPlanCuts;
}
