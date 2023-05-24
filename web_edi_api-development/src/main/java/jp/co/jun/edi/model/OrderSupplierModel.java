package jp.co.jun.edi.model;
import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.OrderCategoryType;
import lombok.Data;
/**
 * 発注メーカー情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSupplierModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** ID. */
    private BigInteger id;
    /** 品番ID. */
    private BigInteger partNoId;
    /** メーカーコード. */
    private String supplierCode;
    /** 発注分類区分. */
    private OrderCategoryType orderCategoryType;
    /** メーカー名. */
    private String supplierName;
    /** 工場コード. */
    private String supplierFactoryCode;
    /** 工場名. */
    private String supplierFactoryName;
    /** 委託先工場名. */
    private String consignmentFactory;
    /** メーカー担当ID. */
    private BigInteger supplierStaffId;
}
