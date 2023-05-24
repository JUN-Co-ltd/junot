package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 優良誤認検査ファイル情報の登録用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationFilePostModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番IDのリスト. */
    private List<BigInteger> items;

    /** ファイルのリスト. */
    private List<FileModel> files;
}
