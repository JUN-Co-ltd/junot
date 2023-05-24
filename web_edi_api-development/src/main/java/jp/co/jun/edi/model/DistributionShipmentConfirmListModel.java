package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * (出荷指示フラグ更新)List用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributionShipmentConfirmListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷指示対象リスト. */
    private List<DistributionShipmentConfirmModel> distributionShipmentConfirms;
}
