package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FileInfoMode;
import lombok.Data;

/**
 * 優良誤認検査ファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationFileModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private BigInteger partNoId;

    /** ファイル情報. */
    private FileModel file;

    /** モード(deleteするか判断する). */
    private FileInfoMode mode;
}
