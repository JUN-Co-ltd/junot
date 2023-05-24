package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FileInfoCategory;
import jp.co.jun.edi.type.FileInfoMode;
import lombok.Data;

/**
 * 品番ファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemFileInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 品番ID. */
    private String partNoId;

    /** ファイルID. */
    private BigInteger fileNoId;

    /**
     * ファイル名.
     * SELECT時のみ使用
     * INSERT/UPDATEでは値をセットしても登録されない
     */
    private String fileName;

    /** ファイル分類(短冊か見積もりか). */
    private FileInfoCategory fileCategory;

    /** ファイル情報テーブルの分類(insertかdeleteか判断する). */
    private FileInfoMode mode;
}
