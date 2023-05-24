package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
/**
 *
 * フクキタル関連の画面表示用のSKU情報モデル.
 *
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenSettingFukukitaruSkuModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** カラーコード. */
    private String colorCode;
    /** カラーコード名. */
    private String colorName;
    /** サイズ. */
    private String size;
    /** 活性(true)/非活性(false). */
    private boolean isEnabled = false;

}
