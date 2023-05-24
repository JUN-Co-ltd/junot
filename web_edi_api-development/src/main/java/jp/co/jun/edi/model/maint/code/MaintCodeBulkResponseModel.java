package jp.co.jun.edi.model.maint.code;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メンテナンスコード情報の一括処理レスポンスModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCodeBulkResponseModel {
    private MaintCountCUDCountModel success;
}
