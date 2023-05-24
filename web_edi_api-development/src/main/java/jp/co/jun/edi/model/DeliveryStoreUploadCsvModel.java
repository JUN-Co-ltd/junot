package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 店舗配分アップロードのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreUploadCsvModel implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 発注No. */
	private String orderNo;

	/** 回数. */
	private String deliveryCount;

	/** 品番. */
	private String partNo;

	//PRD_0120#8343 mod JFE start
//	/** 店舗コード. */
//	private String storeCode;
//
//	/** 色. */
//	private String colorCode;
//
//	/** サイズ. */
//	private String size;
//
//	/** 配分数. */
//	private String deliveryLot;

	/** 店舗. */
	private List<DeliveryStoreUploadCsvStore> stores;
	//PRD_0120#8343 mod JFE end

}
