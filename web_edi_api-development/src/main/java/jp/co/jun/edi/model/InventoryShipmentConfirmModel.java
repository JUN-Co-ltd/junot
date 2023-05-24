package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.InstructorSystemType;
import lombok.Data;

/**
 * 在庫出荷指示情報・LG送信用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryShipmentConfirmModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date cargoAt;

    /** 出荷場所. */
    private String cargoPlace;

    /** 指示元システム. */
    private InstructorSystemType instructorSystem;

    /** 課コード. */
    private String divisionCode;

    /** 品番. */
    private String partNo;

}
