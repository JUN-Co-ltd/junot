package jp.co.jun.edi.model;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * パーツマスタの検索用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemPartSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 検証用. */
    public interface Item {
    };

    @NotEmpty(groups = { Item.class })
    private String itemCode;
}
