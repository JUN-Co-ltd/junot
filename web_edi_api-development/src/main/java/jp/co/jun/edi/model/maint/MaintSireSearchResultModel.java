package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * マスタメンテナンス用の取引先検索結果のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintSireSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** ブランドコード. */
    private String brandCode;

    /** 仕入先コード. */
    private String sireCode;

    /** 仕入先名. */
    private String sireName;

    /** 区分. */
    private String reckbn;

    /** 工場コード. */
    private String kojCode;

    /** 工場名. */
    private String kojName;

    /** 発注区分（生地）. */
    private String hkiji;

    /** 発注区分（製品）. */
    private String hseihin;

    /** 発注区分（値札）. */
    private String hnefuda;

    /** 発注区分（附属）. */
    private String hfuzoku;

    /** 発注書送付先区分. */
    private String hsofkbn;

    /** 納品依頼書送付先区分. */
    private String nsofkbn;

    /** 予備受領書送付先区分. */
    private String ysofkbn;

    /** 有害物質対応区分. */
    private String yugaikbn;
}
