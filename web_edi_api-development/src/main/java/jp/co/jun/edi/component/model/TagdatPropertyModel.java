package jp.co.jun.edi.component.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * TAGDATプロパティ情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagdatPropertyModel implements Serializable {
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

    /** 一時保存ディレクトリパス. */
    @JsonProperty("tmp-directory")
    private String tmpDirectory;

}
