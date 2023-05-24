package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.AllocationType;
import lombok.Data;

/**
 * 納品得意先情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品明細ID. */
    private BigInteger deliveryDetailId;

    /** 店舗コード. */
    private String storeCode;

    /** 店舗別配分率ID. */
    private BigInteger storeDistributionRatioId;

    /** 店舗別配分率区分. */
    private String storeDistributionRatioType;

    /** 店舗別配分率. */
    private BigDecimal storeDistributionRatio;

    /** 配分順. */
    private Integer distributionSort;

    /** 配分区分. */
    private AllocationType allocationType;

    /** 納品得意先SKU情報のリスト. */
    private List<DeliveryStoreSkuModel> deliveryStoreSkus;
}
