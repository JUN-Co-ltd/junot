package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * フクキタル洗濯ネーム付記用語情報のModel.
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruMaterialAppendicesTermModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 付記用語コード. */
    private String appendicesTermCode;

    /** 付記用語コード名. */
    private String appendicesTermCodeName;

    /** 付記用語文章. */
    private String appendicesTermSentence;

    /** 特徴. */
    private String characteristic;

    /** 並び順. */
    private BigInteger sortOrder;

}
