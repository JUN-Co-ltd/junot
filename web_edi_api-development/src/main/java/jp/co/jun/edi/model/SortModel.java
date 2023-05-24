package jp.co.jun.edi.model;

import java.io.Serializable;

import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ソート用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SortModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ソート対象カラム名. */
    private String sortColumnName = "id";

    /**  ソート区分"ASC"or"DESC". */
    private Direction orderByType = Direction.ASC;
}
