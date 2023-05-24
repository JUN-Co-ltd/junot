package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

//PRD_0120#8343 add JFE start
/**
 * 店舗配分アップロードの店舗データ.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreUploadCsvStore implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 店舗コード. */
	private String storeCode;

	/** SKUFormの入力値 */
	private List<DeliveryStoreSkuFormValue> deliveryStoreSkuFormValues;

}
//PRD_0120#8343 add JFE end