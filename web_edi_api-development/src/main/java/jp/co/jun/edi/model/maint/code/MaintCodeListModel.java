package jp.co.jun.edi.model.maint.code;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.Data;

/**
 * メンテナンスコード用のコード一覧Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCodeListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** テーブルID. */
    private MCodmstTblIdType tableId;

    /** マスタ名称. */
    private String name;
}
