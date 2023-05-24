package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * SQリクエスト情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecialtyQubeRequestModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 納品ID. */
    private BigInteger deliveryId;
}
