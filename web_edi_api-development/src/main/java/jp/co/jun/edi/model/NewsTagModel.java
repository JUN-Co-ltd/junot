package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * お知らせ情報タグのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsTagModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** お知らせ情報ID. */
    private BigInteger newsId;

    /** タグ区分. */
    private String tagType;
}
