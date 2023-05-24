package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ブランドコードリスト検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandCodesSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** アカウント名. */
    private String accountName;
}
