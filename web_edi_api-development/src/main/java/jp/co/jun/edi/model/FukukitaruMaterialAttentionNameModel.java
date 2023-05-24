package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FukukitaruMasterMaterialType;
import lombok.Data;

/**
 * フクキタル洗濯ネーム付記用語情報のModel.
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruMaterialAttentionNameModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 資材種別. */
    private FukukitaruMasterMaterialType materialType;

    /** 資材種別名. */
    private String materialTypeName;

    /** 資材コード. */
    private String materialCode;

    /** 資材コード名. */
    private String materialCodeName;

    /** 出荷単位. */
    private Integer moq;

    /** 並び順. */
    private BigInteger sortOrder;

    /** 種類. */
    private String type;

    /** 品名. */
    private String productName;

    /** タイトル. */
    private String title;

    /** アテンション文. */
    private String sentence;

    /** 旧品番. */
    private String oldMaterialCode;

}
