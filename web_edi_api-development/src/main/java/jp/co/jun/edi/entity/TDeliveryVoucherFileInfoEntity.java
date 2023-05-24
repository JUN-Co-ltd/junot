package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.DeliveryVoucherCategoryTypeConverter;
import jp.co.jun.edi.entity.converter.FileInfoStatusTypeConverter;
import jp.co.jun.edi.type.DeliveryVoucherCategoryType;
import jp.co.jun.edi.type.FileInfoStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納品伝票ファイル情報のEntity.
 */
@Entity
@Table(name = "t_delivery_voucher_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryVoucherFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** 伝票分類. */
    @Column(name = "voucher_category")
    @Convert(converter = DeliveryVoucherCategoryTypeConverter.class)
    private DeliveryVoucherCategoryType voucherCategory;

    /** ステータス. */
    @Convert(converter = FileInfoStatusTypeConverter.class)
    private FileInfoStatusType status;
}
