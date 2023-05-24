package jp.co.jun.edi.entity.csv;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.schedule.LinkingCreateCsvFileCommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 直送配分出荷確定ファイル作成用のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class DistributionShipmentConfirmCsvFileEntity extends LinkingCreateCsvFileCommonEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品得意先SKU 日付. */
    @Column(name = "manage_date")
    private Date manageDate;

    /** 納品得意先SKU 時間. */
    @Column(name = "manage_at")
    private Date manageAt;

    /** 納品得意先SKU 管理No. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 納品得意先SKU 行No. */
    @Column(name = "line_number")
    private Integer lineNumber;

    /** 納品明細情報 入荷場所. */
    @Column(name = "arrival_place")
    private String arrivalPlace;

    /**店舗マスタ 配分区分. */
    @Column(name = "distrikind")
    private Integer distrikind;

    /**店舗マスタ 店舗区分. */
    @Column(name = "shopkind")
    private Integer shopkind;

    /**店舗マスタ 店舗形態. */
    @Column(name = "shopfmt")
    private Integer shopfmt;

    /** 納品得意先情報 店舗コード. */
    @Column(name = "store_code")
    private String storeCode;

    /** 納品得意先SKU 出荷伝票No. */
    @Column(name = "shipment_voucher_number")
    private String shipmentVoucherNumber;

    /** 納品得意先SKU 出荷伝票行. */
    @Column(name = "shipment_voucher_line")
    private String shipmentVoucherLine;

    /** 納品情報 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 納品得意先SKU情報 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 納品得意先SKU情報 サイズ. */
    @Column(name = "size")
    private String size;

    /** 納品得意先SKU情報  納品数量. */
    @Column(name = "delivery_lot")
    private Integer deliveryLot;

    /** 納品得意先SKU情報  出荷確定数. */
    @Column(name = "arrival_lot")
    private Integer arrivalLot;

    /** 納品情報 発注No. */
    @Column(name = "order_number")
    private Integer orderNumber;

    /** 納品情報 納品依頼回数. */
    @Column(name = "delivery_count")
    private Integer deliveryCount;

    /** 納品情報 課コード. */
    @Column(name = "division_code")
    private String divisionCode;
}
