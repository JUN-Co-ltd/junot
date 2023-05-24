package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterType;
import jp.co.jun.edi.type.ScreenSettingFukukitaruMasterType;
import lombok.Data;

/**
 * フクキタル関連の画面構成取得検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenSettingFukukitaruSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * マスタタイプリスト.
     * 必須パラメータ。
     * @link {@link FukukitaruMasterType}
     *
     */
    private List<ScreenSettingFukukitaruMasterType> listMasterType;

    /** 品番ID. */
    private BigInteger partNoId;

    /**
     * 発注ID.
     * 発注IDが設定されていない場合、listMasterTypeに以下の設定をしても空リストになる。
     * {@link FukukitaruMasterType.ATTENTION_NAME} value (10)
     * {@link FukukitaruMasterType.WASH_PATTERN} value (13)
     * {@link FukukitaruMasterType.ATTENTION_TAG} value (9)
     * {@link FukukitaruMasterType.HANG_TAG} value (12)
     * {@link FukukitaruMasterType.AUXILIARY_MATERIAL} value (11)
     * {@link FukukitaruMasterType.SKU} value (17)s
     */
    private BigInteger orderId;

    /** デリバリ種別. */
    private FukukitaruMasterDeliveryType deliveryType;


    /**
     * 検索会社名.
     * listMasterTypeに以下が設定されている場合、m_f_address.company_nameに対して部分一致で検索をする。
     * {@link FukukitaruMasterType} BILLING_ADDRESS
     * {@link FukukitaruMasterType} DELIVERY_ADDRESS
     * {@link FukukitaruMasterType} SUPPLIER_ADDRESS
     */
    private String searchCompanyName;

    /**
     * 品種.
     */
    private String partNoKind;

    /** 入力補助セットID. */
    private BigInteger inputAssistId;
}
