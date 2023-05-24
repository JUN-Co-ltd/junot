package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** コンテンツタイプ. */
    private String contentType;

    /** ファイル名. */
    private String fileName;

    /** ファイルの実態. */
    private byte[] fileData;

    /** メモ. */
    private String memo;
}
