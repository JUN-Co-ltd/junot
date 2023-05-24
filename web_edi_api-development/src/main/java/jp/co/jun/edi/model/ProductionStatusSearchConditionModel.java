package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 生産ステータス検索のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductionStatusSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger orderId;
}
