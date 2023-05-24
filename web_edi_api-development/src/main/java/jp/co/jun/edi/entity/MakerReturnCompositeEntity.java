package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.entity.key.MakerReturnCompositeKey;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * メーカー返品一覧情報のEntity.
 */
@Entity
@Table(name = "t_maker_return")
@EntityListeners(AuditingEntityListener.class)
@Data
public class MakerReturnCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private MakerReturnCompositeKey key;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;

    /** 伝票日付(返却日).  */
    @Column(name = "return_at")
    private Date returnAt;

    /** 仕入先 . */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** 仕入先名称 . */
    @Column(name = "name")
    private String supplierName;

    /** 数量. */
    @Column(name = "return_lot")
    private String returnLot;

    /** 金額. */
    @Column(name = "unit_price")
    private String unitPrice;

    /** 発注番号. */
    @Column(name = "order_number")
    private String orderNumber;

    /** 伝票入力日. */
    @Column(name = "created_at")
    private Date createdAt;
}
