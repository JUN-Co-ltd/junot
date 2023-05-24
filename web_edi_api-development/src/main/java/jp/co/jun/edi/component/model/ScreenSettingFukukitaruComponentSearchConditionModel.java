package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import jp.co.jun.edi.component.ScreenSettingFukukitaruComponent;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.ScreenSettingFukukitaruMasterType;
import lombok.Data;

/**
 *
 * {@link ScreenSettingFukukitaruComponent} の パラメータモデル.
 *
 */
@Data
public class ScreenSettingFukukitaruComponentSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * マスタタイプリスト.
     */
    private List<ScreenSettingFukukitaruMasterType> listMasterType;

    /**
     * 品番ID.
     */
    private BigInteger partNoId;

    /**
     * 発注ID.
     */
    private BigInteger orderId;

    /**
     * デリバリ.
     * */
    private FukukitaruMasterDeliveryType deliveryType;

    /**
     * 検索会社名.
     */
    private String searchCompanyName;

    /**
     * 品種.
     */
    private String partNoKind;

    /**
     * 発注種類.
     */
    private FukukitaruMasterOrderType orderType;

    /**
     * ログインユーザが所属する会社コード.
     */
    private String company;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード. */
    private String itemCode;

    /** 入力補助セットID. */
    private BigInteger inputAssistId;

}
