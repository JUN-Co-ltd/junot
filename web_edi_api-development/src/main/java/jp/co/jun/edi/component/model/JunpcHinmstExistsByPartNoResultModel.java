package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムの品番マスタの存在チェック結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcHinmstExistsByPartNoResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private int recordCnt;
}
