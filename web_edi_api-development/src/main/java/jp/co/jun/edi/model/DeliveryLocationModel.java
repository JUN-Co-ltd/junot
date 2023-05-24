// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 納入場所マスタのModel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryLocationModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 物流コード. */
    private String logisticsCode;

    /** 場所名称. */
    private String companyName;

}
// PRD_0123 #7054 add JFE end