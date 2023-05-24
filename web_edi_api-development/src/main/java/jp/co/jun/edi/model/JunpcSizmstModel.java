package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムのサイズマスタ.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcSizmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String hscd;
    private String szkg;
    private String jun;
    //    private String mntflg;
    //    private String crtymd;
    //    private String updymd;
    //    private String tanto;
    //    private String souflg;
    //    private String souymd;
}
