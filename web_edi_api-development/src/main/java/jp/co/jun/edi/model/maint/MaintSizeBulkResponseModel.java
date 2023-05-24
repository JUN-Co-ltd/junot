package jp.co.jun.edi.model.maint;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.model.maint.code.MaintCountCUDCountModel;
import lombok.Data;

/**
 * メンテナンスコード情報の一括処理レスポンスModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintSizeBulkResponseModel {
    private MaintCountCUDCountModel success;
}
