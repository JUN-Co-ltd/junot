package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 在庫出荷指示情報の受信データ取得用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryShipmentConfirmListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷指示対象リスト. */
    private List<InventoryShipmentConfirmModel> inventoryShipmentConfirms;
}
