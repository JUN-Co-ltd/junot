package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
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
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.CarryTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CarryType;
import lombok.Data;

/**
 * 納品得意先情報の配布先指示Entity.
 */
@Entity
@Table(name = "t_delivery_store")
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedDistributionShipmentSearchResultEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 出荷指示日. */
    @Column(name = "shipping_instructions_at")
    private Date shippingInstructionsAt;

    /** 入荷フラグ. */
    @Column(name = "arrival_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType arrivalFlg;

    /** 入荷日. */
    @Column(name = "arrival_at")
    private Date arrivalAt;

    /** 納品依頼日. */
    @Column(name = "delivery_request_at")
    private Date deliveryRequestAt;

    /** 送信区分. */
    @Column(name = "send_code")
    private String sendCode;

    /** 納品No. */
    @Column(name = "delivery_number")
    private BigInteger deliveryNumber;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** キャリー区分. */
    @Column(name = "carry_type")
    @Convert(converter = CarryTypeConverter.class)
    private CarryType carryType;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 出荷指示済フラグ. */
    @Column(name = "shipping_instructions_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType shippingInstructionsFlg;

    /** 仕入確定数. */
    @Column(name = "fix_arrival_lot_sum")
    private int fixArrivalLotSum;

    /** 数量. */
    @Column(name = "delivery_lot_sum")
    private Integer deliveryLotSum;
    /** 上代金額. */
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    /** 製品名. */
    @Column(name = "product_name")
    private String productName;
}
