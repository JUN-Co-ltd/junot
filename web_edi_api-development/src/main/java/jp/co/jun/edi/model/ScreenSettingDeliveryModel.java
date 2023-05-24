package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品依頼関連の画面構成情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenSettingDeliveryModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番情報. */
    private ItemModel item;

    /** 発注情報. */
    private OrderModel order;

    /** 納品依頼情報. */
    private DeliveryModel delivery;

    /** 過去納品依頼情報. */
    private List<DeliveryModel> deliveryHistory;

    /** 閾値. */
    private BigDecimal threshold;

    /** 店舗別配分率. */
    private List<JunpcStoreHrtmstModel> storeHrtmstList;

    /** 店舗マスタ. */
    private List<JunpcTnpmstModel> tnpmstList;

    // PRD_0031 add SIT start
    /** 在庫数. */
    private List<ShopStockModel> shopStockList;
    // PRD_0031 add SIT end
    // PRD_0033 add SIT start
    /** 売上数. */
    private List<PosOrderDetailModel> posOrderDetailList;
    // PRD_0033 add SIT end
	// PRD_0123 #7054 add JFE start
    /** 納入場所*/
    private List<DeliveryLocationModel> deliveryLocationList;
	// PRD_0123 #7054 add JFE end
}
