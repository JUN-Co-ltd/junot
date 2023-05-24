package jp.co.jun.edi.entity.extended;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.entity.converter.PurchaseDataTypeConverter;
import jp.co.jun.edi.entity.converter.PurchaseTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.PurchaseDataType;
import jp.co.jun.edi.type.PurchaseType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張仕入情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTPurchaseEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    // 仕入情報
    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;

    /** 日付:SQの管理情報. */
    @Column(name = "sq_manage_date")
    private Date sqManageDate;

    /** 時間:SQの管理情報. */
    @Column(name = "sq_manage_at")
    private Date sqManageAt;

    /** 管理No:SQの管理情報. */
    @Column(name = "sq_manage_number")
    private String sqManageNumber;

    /** 行No:SQの管理情報. */
    @Column(name = "line_number")
    private Integer lineNumber;

    /** データ種別:SR、KR. */
    @Column(name = "data_type")
    @Convert(converter = PurchaseDataTypeConverter.class)
    private PurchaseDataType dataType;

    /** 仕入区分. */
    @Column(name = "purchase_type")
    @Convert(converter = PurchaseTypeConverter.class)
    private PurchaseType purchaseType;

    /** 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    /** 入荷店舗. */
    @Column(name = "arrival_shop")
    private String arrivalShop;

    /** 仕入先. */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** 製品工場. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 計上日. */
    @Column(name = "record_at")
    private Date recordAt;

    /** 仕入相手伝票No. */
    @Column(name = "maker_voucher_number")
    private String makerVoucherNumber;

    /** 仕入伝票No. */
    @Column(name = "purchase_voucher_number")
    private String purchaseVoucherNumber;

    /** 仕入伝票行. */
    @Column(name = "purchase_voucher_line")
    private Integer purchaseVoucherLine;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    private String size;

    /** 入荷数. */
    @Column(name = "arrival_count")
    private Integer arrivalCount;

    /** 入荷確定数. */
    @Column(name = "fix_arrival_count")
    private Integer fixArrivalCount;

    /** 良品・不用品区分. */
    @Column(name = "non_conforming_product_type")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType nonConformingProductType;

    /** 指示番号. */
    @Column(name = "instruct_number")
    private String instructNumber;

    /** 指示番号行. */
    @Column(name = "instruct_number_line")
    private Integer instructNumberLine;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注番号. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 引取回数: 納品明細の納品回数に相当. */
    @Column(name = "purchase_count")
    private Integer purchaseCount;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 仕入単価. */
    @Column(name = "purchase_unit_price")
    private Integer purchaseUnitPrice;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;

    // 発注情報
    /** 発注情報存在チェック用の発注ID. */
    @Column(name = "check_order_id")
    private BigInteger checkOrderId;

    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;
}
