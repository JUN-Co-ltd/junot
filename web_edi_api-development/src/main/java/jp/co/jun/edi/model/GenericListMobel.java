package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * @param <T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericListMobel<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 結果リスト. */
    private List<T> items;

    /** この結果の次のページにアクセスするためのトークン.次のページがない場合は省略されます. */
    private String nextPageToken;

    /** 改訂日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date revisionedAt;
}
