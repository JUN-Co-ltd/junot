package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 店舗情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 店舗コード. */
    private String storeCode;

    /** 課コード. */
    private String divisionCode;
}
