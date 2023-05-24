package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 閾値マスタの検索用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThresholdSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ブランド. */
    private String brandCode;

    /** アイテム. */
    private String itemCode;
}
