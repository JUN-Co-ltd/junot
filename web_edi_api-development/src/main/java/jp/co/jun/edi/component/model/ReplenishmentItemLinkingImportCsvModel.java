package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * 補充対象品番データ取込用CSVファイルModel.
 */
@Data
public class ReplenishmentItemLinkingImportCsvModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 管理情報日付. */
    private String manageDate;
    /** 管理情報時間. */
    private String manageAt;
    /** 管理情報 SEQ. */
    private String sequence;
    /** 品番. */
    private String partNo;
    /** 登録日. */
    private String createdAt;
    /** 修正日. */
    private String updatedAt;
    /** 入力者. */
    private String tanto;
}
