package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.DeliveryVoucherCategoryType;
import jp.co.jun.edi.type.FileInfoStatusType;
import lombok.Data;

/**
 * 納品伝票ファイル情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryVoucherFileInfoModel implements Serializable {
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

    /** 伝票分類. */
    private DeliveryVoucherCategoryType voucherCategory;

    /** ステータス. */
    private FileInfoStatusType status;
}
