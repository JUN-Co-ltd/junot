package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.InstructorSystemType;
import lombok.Data;

/**
 * 在庫出荷指示データの検索結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryShipmentSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date cargoAt;

    /** 指示元システム. */
    private InstructorSystemType instructorSystem;

    /** 出荷場所. */
    private String cargoPlace;

    /** ブランドコード. */
    private String brandCode;

    /** ブランド名. */
    private String brandName;

    /** 課コード. */
    private String divisionCode;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 数量. */
    private Integer deliveryLotSum;

    /** 上代金額. */
    private BigDecimal retailPriceSum;

    /** LG送信区分. */
    private String lgSendType;

}
