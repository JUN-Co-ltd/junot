// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納入場所検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryLocationSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger id; //品番Id
}
// PRD_0123 #7054 add JFE end