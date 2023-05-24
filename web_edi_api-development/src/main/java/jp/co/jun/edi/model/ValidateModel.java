package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * バリデーション結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ValidateModel extends GenericModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ErrorDetailModel> errors;

}
