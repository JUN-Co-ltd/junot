package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * フクキタル品番情報のModel.
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruItemModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** カテゴリコード（VIS、アダム：空文字（カテゴリコードなし）固定）. */
    private BigInteger categoryCode;

    /** NERGY用メリット下札コード1（VIS：空文字固定）. */
    private String nergyBillCode1;

    /** NERGY用メリット下札コード2（VIS：空文字固定）. */
    private String nergyBillCode2;

    /** NERGY用メリット下札コード3（VIS：空文字固定）. */
    private String nergyBillCode3;

    /** NERGY用メリット下札コード4（VIS：空文字固定）. */
    private String nergyBillCode4;

    /** NERGY用メリット下札コード5（VIS：空文字固定）. */
    private String nergyBillCode5;

    /** NERGY用メリット下札コード6（VIS：空文字固定）. */
    private String nergyBillCode6;

    /**
     * シールへの付記用語印字.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printAppendicesTerm;

    /**
     * 原産国印字.
     * false：印字しない(0)、true:印字する(1)
     * */
    private BooleanType printCoo;

    /**
     * シールへの品質印字.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printParts;

    /**
     * QRコードの有無.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printQrcode;

    /**
     * 洗濯ネームサイズ印字.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printSize;

    /**
     * シールへの絵表示印字.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printWashPattern;

    /**
     * シールへのリサイクルマーク印字.
     * false：印字しない(0)、true:印字する(1)
     */
    private BigInteger recycleMark;

    /** REEFUR用ブランド（VIS：空文字固定）. */
    private String reefurPrivateBrandCode;

    /** サタデーズサーフ用NY品番（VIS：空文字固定）. */
    private String saturdaysPrivateNyPartNo;

    /** アテンションシールのシール種類. */
    private BigInteger stickerTypeCode;

    /** 洗濯ネームテープ種類. */
    private BigInteger tapeCode;

    /** 洗濯ネームテープ巾. */
    private BigInteger tapeWidthCode;

    /** 中国内版情報製品分類. */
    private BigInteger cnProductCategory;

    /** 中国内版情報製品種別. */
    private BigInteger cnProductType;

    /** 洗濯ネームテープ種類名称. */
    private String tapeName;

    /** 洗濯ネームテープ巾名称. */
    private String tapeWidthName;

    /** シールへのリサイクルマーク印字名称. */
    private String recycleName;

    /** アテンションシールのシール種類名称. */
    private String sealName;

    /** 中国内版情報製品分類名称. */
    private String productCategoryName;

    /** 中国内版情報製品種別名称. */
    private String productTypeName;

    /**
     * サスティナブルマーク印字0：印字しない,1：印字する.
     * false：印字しない(0)、true:印字する(1)
     */
    private BooleanType printSustainableMark;

    /** 洗濯ネーム付記用語. */
    private List<FukukitaruItemWashAppendicesTermModel> listItemWashAppendicesTerm;

    /** アテンションタグ付記用語. */
    private List<FukukitaruItemAttentionAppendicesTermModel> listItemAttentionAppendicesTerm;

    /** 絵表示. */
    private List<FukukitaruItemWashPatternModel> listItemWashPattern;
}
