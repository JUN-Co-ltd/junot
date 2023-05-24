package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 仕入確定(LG送信)用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseConfirmListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 仕入確定対象リスト. */
    private List<PurchaseConfirmModel> purchases;
}
