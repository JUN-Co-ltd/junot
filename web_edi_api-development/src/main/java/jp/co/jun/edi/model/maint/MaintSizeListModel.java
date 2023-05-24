//PRD_0137 #10669 mod start
package jp.co.jun.edi.model.maint;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メンテナンスコード用のコード一覧Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintSizeListModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID.    */
    private String id;

    /** 表示順. */
    private String jun;

    /** サイズ. */
    private String szkg;
}
//PRD_0137 #10669 add end