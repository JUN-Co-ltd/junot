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
public class FukukitaruItemAttentionAppendicesTermModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** フクキタル用アテンションタグ付記用語情報ID. */
    private BigInteger id;

    /** 付記用語ID. */
    private BigInteger appendicesTermId;

    /** カラーコード. */
    private String colorCode;

    /** 付記用語コード. */
    private String appendicesTermCode;

    /** 付記用語コード名. */
    private String appendicesTermCodeName;
}
