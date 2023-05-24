package jp.co.jun.edi.model.maint.code;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メンテナンスコード用の検索結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCodeSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Map<String, String>> items;
}
