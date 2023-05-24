package jp.co.jun.edi.model.maint.code;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jp.co.jun.edi.model.ScreenSettingStructureModel;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Data;

/**
 * メンテナンスコード用の画面構成Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCodeScreenSettingModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** テーブル区分. */
    private MCodmstTblIdType tableId;

    /** 画面構成定義（JSON形式データ）. */
    @JsonProperty("fields")
    private List<ScreenSettingStructureModel> structure;

    /** 登録日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date createdAt;

    /** 登録ユーザID. */
    private BigInteger createdUserId;

    /** 更新日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date updatedAt;

    /** 更新ユーザID. */
    private BigInteger updatedUserId;

    /** 削除日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date deletedAt;

}
