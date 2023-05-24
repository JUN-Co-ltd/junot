//PRD_0137 #10669 add start
package jp.co.jun.edi.model.maint;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * サイズ情報の登録／更新用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintSizeBulkUpdateModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 改訂日時. */
    private Date revisionedAt;
    /** 品種コード. */
    private String hscd;
    /** データリスト. */
    private List<Map<String, String>> items;
}
//PRD_0137 #10669 add end