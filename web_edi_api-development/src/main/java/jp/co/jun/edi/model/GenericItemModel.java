package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * @param <T>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericItemModel<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 結果. */
    private T item;

    /** 改訂日時. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date revisionedAt;
}
