package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品得意先SKU情報のEntity.
 */
@Entity
@Table(name = "t_delivery_store_sku")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryStoreSkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;

    /** 納品得意先ID. */
    @Column(name = "delivery_store_id")
    private BigInteger deliveryStoreId;

    /** 日付:WMS管理用. */
    @Temporal(TemporalType.DATE)
    @Column(name = "manage_date")
    private Date manageDate;

    /** 時間:WMS管理用. */
    @Temporal(TemporalType.TIME)
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理No:WMS管理用. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 行No:WMS管理用. */
    @Column(name = "line_number")
    private Integer lineNumber;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 納品数量. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 出荷確定数. */
    @Column(name = "arrival_lot")
    private int arrivalLot;

    /** 出荷伝票No. */
    @Column(name = "shipment_voucher_number")
    private String shipmentVoucherNumber;

    /** 出荷伝票行. */
    @Column(name = "shipment_voucher_line")
    private Integer shipmentVoucherLine;
}
