package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import lombok.Data;


/**
 * 取込用仕入情報のModel.
 */
@Data
public class PurchaseImportModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 日付:SQの管理情報. */
    private Date sqManageDate;

    /** 時間:SQの管理情報. */
    private Date sqManageAt;

    /** 管理No:SQの管理情報. */
    private String sqManageNumber;

    /** 行No:SQの管理情報. */
    private Integer lineNumber;

    /** データ種別:SR、KR. */
    private PurchaseDataType dataType;

    /** 仕入区分. */
    private PurchaseType purchaseType;

    /** 入荷場所. */
    private String arrivalPlace;

    /** 入荷店舗. */
    private String arrivalShop;

    /** 仕入先. */
    private String supplierCode;

    /** 製品工場. */
    private String mdfMakerFactoryCode;

    /** 入荷日. */
    private Date arrivalAt;

    /** 計上日. */
    private Date recordAt;

    /** 仕入相手伝票No. */
    private String makerVoucherNumber;

    /** 仕入伝票No. */
    private String purchaseVoucherNumber;

    /** 仕入伝票行. */
    private Integer purchaseVoucherLine;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 色. */
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 入荷数. */
    private Integer arrivalCount;

    /** 入荷確定数. */
    private Integer fixArrivalCount;

    /** 良品・不用品区分. */
    private BooleanType nonConformingProductType;

    /** 指示番号. */
    private String instructNumber;

    /** 指示番号行. */
    private Integer instructNumberLine;

    /** 発注ID. */
    private BigInteger orderId;

    /** 発注番号. */
    private BigInteger orderNumber;

    /** 引取回数: 納品明細の納品回数に相当. */
    private Integer purchaseCount;

    /** 課コード. */
    private String divisionCode;

    /** 仕入単価. */
    private Integer purchaseUnitPrice;

    /** 納品ID. */
    private BigInteger deliveryId;

    /** LG送信区分. */
    private LgSendType lgSendType;
}
