package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * デリスケ納品依頼情報検索用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelischeDeliveryRequestSearchConditionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 発注ID. */
    private BigInteger orderId;

    /** 納品日from. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtFrom;

    /** 納品日to. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtTo;

    /** 納品日from(年度・納品週から作成). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtFromByMdweek;

    /** 納品日to(年度・納品週から作成). */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date deliveryAtToByMdweek;

    /** 納品遅れ. */
    private boolean deliveryAtLateFlg;
}
