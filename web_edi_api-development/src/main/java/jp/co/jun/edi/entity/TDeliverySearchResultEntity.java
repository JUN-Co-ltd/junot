package jp.co.jun.edi.entity;

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

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteOrderTypeConverter;
import jp.co.jun.edi.entity.converter.CompleteTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import lombok.Data;

/**
 * 配分一覧(納品依頼)検索結果のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class TDeliverySearchResultEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 納品依頼ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 承認ステータス. */
    @Column(name = "delivery_approve_status")
    private String deliveryApproveStatus;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 納品日(修正納期). */
    @Column(name = "correction_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date correctionAt;

    /** 店舗別登録済フラグ. */
    @Column(name = "store_registered_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType storeRegisteredFlg;

    // PRD_0087 mod SIT start
    ///** 配分確定フラグ. */
    //@Column(name = "allocation_confirm_flg")
    //@Convert(converter = BooleanTypeConverter.class)
    //private BooleanType allocationConfirmFlg;
    /** 配分完了日. */
    @Column(name = "allocation_complete_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationCompleteAt;

    /** 配分計上日. */
    @Column(name = "allocation_record_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private Date allocationRecordAt;
    // PRD_0087 mod SIT end

    /** 発注数量. */
    private BigDecimal quantity;

    /** 製品完納区分. */
    @Convert(converter = CompleteOrderTypeConverter.class)
    @Column(name = "product_complete_order")
    private CompleteOrderType productCompleteOrder;

    /** 全済区分. */
    @Convert(converter = CompleteTypeConverter.class)
    @Column(name = "all_completion_type")
    private CompleteType allCompletionType;

    /** 取引数(納品SKUの納品数量の合計). */
    @Column(name = "transaction_lot")
    private int transactionLot;

    /** 配分数(納品得意先SKUの納品数量の合計). */
    @Column(name = "allocation_lot")
    private int allocationLot;

    //PRD_0127 #9837 add JFE start
    /** 納品先. */
    @Column(name = "company_name")
    private String companyName;
    //PRD_0127 #9837 add JFE end

    /** 仕入確定数. */
    @Column(name = "fix_arrival_count")
    private int fixArrivalCount;

    /**
     * 入荷フラグ.
     * false：未入荷(0)、true:入荷済(1)
     */
    @Column(name = "arrival_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType arrivalFlg;

    /**
     * 出荷指示フラグ.
     * false：未出荷(0)、true:出荷済(1)
     */
    @Column(name = "shipping_instructions_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType shippingInstructionsFlg;
}
