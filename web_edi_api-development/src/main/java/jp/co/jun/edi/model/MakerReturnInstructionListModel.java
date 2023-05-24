package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メーカー返品指示(LG送信)用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MakerReturnInstructionListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** メーカー返品指示確定対象リスト. */
    private List<MakerReturnInstructionModel> makerReturnConfirms;
}
