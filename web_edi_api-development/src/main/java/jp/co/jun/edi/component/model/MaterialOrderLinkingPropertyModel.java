package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 資材発注連携用プロパティ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaterialOrderLinkingPropertyModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** メール：FROM. */
    @JsonProperty("mail-from")
    private String mailFrom;

    /** メール：TO. */
    @JsonProperty("mail-to")
    private String mailTo;

    /** メール：CC. */
    @JsonProperty("mail-cc")
    private String mailCc;

    /** メール：BCC. */
    @JsonProperty("mail-bcc")
    private String mailBcc;

    /** メール：送信フラグ. */
    @JsonProperty("mail-send")
    private boolean mailSend;

    /** 一時保存ディレクトリパス. */
    @JsonProperty("tmp-directory")
    private String tmpDirectory;

    /** S3:プレフィックス. */
    @JsonProperty("s3-prefix")
    private String s3Prefix;

}
