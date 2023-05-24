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

import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.entity.schedule.LinkingCreateCsvFileCommonEntity;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * メーカー返品指示バッチ用拡張メーカー返品情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTMakerReturnLinkingCsvFileEntity extends LinkingCreateCsvFileCommonEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 伝票番号. */
    @Column(name = "voucher_number")
    private String voucherNumber;

    /** 伝票番号行. */
    @Column(name = "voucher_line")
    private Integer voucherLine;

    // PRD_0089 add SIT start
    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;
    // PRD_0089 add SIT end

    /** 日付. */
    @Column(name = "manage_date")
    private Date manageDate;

    /** 時間. */
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理No. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 行No. */
    private Integer sequence;

    /** 店舗コード. */
    private String shpcd;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 仕入先コード. */
    @Column(name = "supplier_code")
    private String supplierCode;

    // PRD_0089 add SIT start
    /** 生産工場. */
    @Column(name = "mdf_maker_factory_code")
    private String mdfMakerFactoryCode;
    // PRD_0089 add SIT end

    /** 返品日. */
    @Column(name = "return_at")
    private Date returnAt;

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

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注番号. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 返品数量. */
    @Column(name = "return_lot")
    private Integer returnLot;

    /** 摘要. */
    private String memo;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;

    /** 単価. */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    // PRD_0089 add SIT start
    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;
    // PRD_0089 add SIT start

    /** プロパー掛率. */
    @Column(name = "proper_rate")
    private Integer properRate;

    /** B級品単価. */
    @Column(name = "non_conforming_product_unit_price")
    private BigDecimal nonConformingProductUnitPrice;

    /** 指示番号. */
    @Column(name = "instruct_number")
    private String instructNumber;

    /** 指示番号行. */
    @Column(name = "instruct_number_line")
    private Integer instructNumberLine;

}
