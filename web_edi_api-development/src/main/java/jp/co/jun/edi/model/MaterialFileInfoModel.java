package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FukukitaruMasterType;
import lombok.Data;

/**
 * フクキタル用発注画面のダウンロードファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaterialFileInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** フクキタルマスタ種別. */
    private FukukitaruMasterType masterType;

    /** ファイルID. */
    private BigInteger fileNoId;

}
