package jp.co.jun.edi.model.maint.code;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * メンテナンスコード用の登録／更新用Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintCodeBulkUpdateModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 改訂日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date revisionedAt;
    /** データリスト. */
    private List<Map<String, String>> items;
}
