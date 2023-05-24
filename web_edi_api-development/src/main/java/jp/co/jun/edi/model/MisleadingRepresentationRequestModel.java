package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 優良誤認 承認Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MisleadingRepresentationRequestModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 優良誤認承認情報. */
    private List<MisleadingRepresentationModel> misleadingRepresentations;
}

