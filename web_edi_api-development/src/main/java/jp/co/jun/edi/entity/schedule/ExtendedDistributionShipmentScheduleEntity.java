package jp.co.jun.edi.entity.schedule;

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

import jp.co.jun.edi.entity.converter.PsTypeConverter;
import jp.co.jun.edi.entity.converter.SuspendTypeConverter;
import jp.co.jun.edi.type.PsType;
import jp.co.jun.edi.type.SuspendType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配分出荷指示ファイル作成用Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedDistributionShipmentScheduleEntity extends LinkingCreateCsvFileCommonEntity {
    private static final long serialVersionUID = 1L;

    // 配分出荷指示情報
    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 日付:WMS管理用. */
    @Column(name = "manage_date")
    private Date manageDate;

    /** 時間:WMS管理用. */
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理No:WMS管理用. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 行No:WMS管理用. */
    @Column(name = "line_number")
    private Integer lineNumber;

    /** 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    /** 配分順. */
    @Column(name = "hjun")
    private Integer hjun;

    /** 保留区分. */
    @Column(name = "suspend_type")
    @Convert(converter = SuspendTypeConverter.class)
    private SuspendType suspendType;

    /** 店舗コード. */
    @Column(name = "store_code")
    private String storeCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 納品数量. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** 掛率. */
    @Column(name = "percent")
    private BigDecimal percent;

    /** 下代. */
    @Column(name = "under_retail_price")
    private BigDecimal underRetailPrice;

    /** PS区分. */
    @Column(name = "ps_type")
    @Convert(converter = PsTypeConverter.class)
    private PsType psType;

    /** セール上代. */
    @Column(name = "sale_retail_price")
    private BigDecimal saleRetailPrice;

    /** OFF率. */
    @Column(name = "off_percent")
    private BigDecimal offPercent;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;
}
