package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Convert;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.DistributionShipmentSendType;
import lombok.Data;

/**
 * 配分一覧(納品情報)の検索結果Model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DistributionShipmentSearchResultModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品明細ID. */
    private BigInteger id;

    /** 出荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date shippingInstructionsAt;

    /** 入荷フラグ. */
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType arrivalFlg;

    /** 入荷日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date arrivalAt;

    /** 納品依頼日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryRequestAt;

    /** 送信状態. */
    private DistributionShipmentSendType sendStatus;

    /** 発注No. */
    private BigInteger orderNumber;

    /** 納品依頼ID. */
    private BigInteger deliveryNumber;

    /** 納品依頼回数. */
    private int deliveryCount;

    /** 課コード. */
    private String divisionCode;

    /** キャリー区分. */
    private CarryType carryType;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 仕入確定数. */
    private int fixArrivalLotSum;

    /** 数量. */
    private Integer deliveryLotSum;

    /** 上代金額. */
    private BigDecimal retailPriceSum;
}
