package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品依頼ファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryFileInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** 納品依頼回数. */
    private int deliveryCount;

    /** 発注ID. */
    private BigInteger orderId;

    /** ファイルID. */
    private BigInteger fileNoId;

    /** 公開日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date publishedAt;

    /** 公開終了日. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date publishedEndAt;
}
