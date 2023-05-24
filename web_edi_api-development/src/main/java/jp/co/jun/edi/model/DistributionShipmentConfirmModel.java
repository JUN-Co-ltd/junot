package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 出荷指示送信用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributionShipmentConfirmModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品明細ID. */
    private BigInteger id;
}
