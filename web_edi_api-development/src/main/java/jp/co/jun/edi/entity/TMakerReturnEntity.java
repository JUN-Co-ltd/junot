package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * メーカー返品情報のEntity.
 */
@Entity
@Table(name = "t_maker_return")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TMakerReturnEntity extends GenericDeletedAtUnixEntity {
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

    /** 倉庫連携ファイルID. */
    @Column(name = "wms_linking_file_id")
    private BigInteger wmsLinkingFileId;

    /** 日付. */
    @Temporal(TemporalType.DATE)
    @Column(name = "manage_date")
    private Date manageDate;

    /** 時間. */
    @Temporal(TemporalType.TIME)
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理番号. */
    @Column(name = "manage_number")
    private String manageNumber;

    /** 行No:WMS管理用. */
    @Column(name = "sequence")
    private Integer sequence;

    /** 店舗コード. */
    @Column(name = "shpcd")
    private String shpcd;

    /** 物流コード. */
    @Column(name = "logistics_code")
    private String logisticsCode;

    /** 仕入先コード. */
    @Column(name = "supplier_code")
    private String supplierCode;

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
    @Column(name = "size")
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

    /** 返品確定数. */
    @Column(name = "fix_return_lot")
    private Integer fixReturnLot;

    /** 摘要. */
    @Column(name = "memo")
    private String memo;

    /** 製造担当コード. */
    @Column(name = "mdf_staff_code")
    private String mdfStaffCode;

    /** 指示番号. */
    @Column(name = "instruct_number")
    private String instructNumber;

    /** 指示番号行. */
    @Column(name = "instruct_number_line")
    private Integer instructNumberLine;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;
}
