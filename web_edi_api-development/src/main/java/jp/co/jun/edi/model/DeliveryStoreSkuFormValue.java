package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

//PRD_0120#8343 add JFE start
/**
 * 店舗配分アップロードのSKUFormの入力値.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryStoreSkuFormValue implements Serializable{

	private static final long serialVersionUID = 1L;


	/** カラーコード. */
    private String colorCode;

    /** サイズ. */
    private String size;

	/** 納品数量. */
	private String deliveryLot;
}
//PRD_0120#8343 add JFE end
