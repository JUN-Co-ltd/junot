package jp.co.jun.edi.model.maint.code;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 登録、更新、削除の件数Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCountCUDCountModel {
    /** 更新件数. */
    private Integer updated;

    /** 登録件数. */
    private Integer registed;

    /** 削除件数. */
    private Integer deleted;
}
