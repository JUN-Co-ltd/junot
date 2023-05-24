package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 資材発注連携エラーメール転送用プロパティ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaterialOrderLinkingErrorMailForwardPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** S3のエラーフォルダのプレフィックス. */
    private String errorPrefix;

    /** S3の転送済みフォルダのプレフィックス. */
    private String forwardedPrefix;

    /** S3の受信済みフォルダのプレフィックス. */
    private String receivedPrefix;

    /** メール：FROM. */
    @JsonProperty("mail-from")
    private String mailFrom;

    /** メール：CC. */
    @JsonProperty("mail-cc")
    private String mailCc;

    /** メール：BCC. */
    @JsonProperty("mail-bcc")
    private String mailBcc;

    /** メール：送信フラグ. */
    @JsonProperty("mail-send")
    private boolean mailSend;
}
