package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 出荷関連プロパティ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipmentPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷データ一時保存先フォルダパス. */
    private String shipmentTmpDirectory;

    /** 出荷データ最大処理件数. */
    private int shipmentMaxProcesses;

    /** 出荷データS3プレフィックス. */
    private String shipmentS3Prefix;

    /** システム名. */
    private String systemName;
}
