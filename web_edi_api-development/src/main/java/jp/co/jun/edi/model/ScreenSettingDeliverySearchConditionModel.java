package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.ScreenSettingDeliveryMasterType;
import lombok.Data;

/**
 * 納品依頼関連の画面構成取得検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenSettingDeliverySearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** マスタタイプリスト. */
    private List<ScreenSettingDeliveryMasterType> listMasterType;

    // PRD_0031 add SIT start
    /** 品番. */
    private String partNo;
    // PRD_0031 add SIT end

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;

    /** シーズン. */
    private String seasonCode;

    /** 納品得意先に登録されている店舗コード・課コードリスト. */
    private List<DeliveryStoreInfoModel> deliveryStoreInfos;
    //PRD_0123 #7054 JFE add start
    /**品番情報.id. */
    private BigInteger id;
    //PRD_0123 #7054 JFE add end
}
