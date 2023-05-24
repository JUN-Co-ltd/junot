package jp.co.jun.edi.entity;

import java.io.Serializable;
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

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.CarryTypeConverter;
import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * 仕入一覧検索結果格納用のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PurchaseCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID(納品明細ID). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品No. */
    @Column(name = "delivery_number")
    private String deliveryNumber;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** キャリー区分. */
    @Column(name = "carry_type")
    @Convert(converter = CarryTypeConverter.class)
    private CarryType carryType;

    /** 仕入状態(仕入ステータス). */
    @Column(name = "arrival_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType arrivalFlg;

    /** 納品日. */
    @Column(name = "correction_at")
    private Date correctionAt;

    /** 仕入先コード. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 仕入先名. */
    @Column(name = "mdf_maker_name")
    private String mdfMakerName;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 配分数. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 仕入(入荷)数合計(納品明細基準). */
    @Column(name = "arrival_count_sum")
    private int arrivalCountSum;

    /** 仕入(入荷)確定数合計(納品明細基準). */
    @Column(name = "fix_arrival_count_sum")
    private int fixArrivalCountSum;

    /** 仕入登録済数. */
    @Column(name = "purchase_registered_count")
    private int purchaseRegisteredCount;

    /** 仕入指示送信済数. */
    @Column(name = "purchase_confirmed_count")
    private int purchaseConfirmedCount;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;
}
