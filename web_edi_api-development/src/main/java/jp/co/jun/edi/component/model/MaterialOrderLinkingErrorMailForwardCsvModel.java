package jp.co.jun.edi.component.model;

import lombok.Data;
/**
 * 資材発注連携エラーメール転送CSVモデル.
 */
@Data
public class MaterialOrderLinkingErrorMailForwardCsvModel {
    /** オーダー識別コード. */
    private String orderCode;
    /** 資材発注連携受信日時. */
    private String receivedAt;
    /** 資材発注連携取込日時. */
    private String takeInAt;
    /** エラーコード. */
    private String errorCode;
    /** エラーメッセージ. */
    private String errorMessage;
}
