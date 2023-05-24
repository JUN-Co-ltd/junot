package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムのコードマスタのタイプ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcCodmastTypeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<JunpcCodmstModel> type1s;

    private List<JunpcCodmstModel> type2s;

    private List<JunpcCodmstModel> type3s;
}
