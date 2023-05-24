package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張発注SKU情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTOrderSkuEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注Id. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注No. */
    @Column(name = "order_number")
    private BigInteger orderNumber;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** サイズ構成比. */
    @Column(name = "size_composition_ratio")
    private int sizeCompositionRatio;

    /** 製品発注数. */
    @Column(name = "product_order_lot")
    private int productOrderLot;

    /** 製品裁断数. */
    @Column(name = "product_cut_lot")
    private int productCutLot;

    /** 納品依頼数量. */
    @Column(name = "delivery_lot")
    private int deliveryLot;

    /** 入荷数量. */
    @Column(name = "arrival_lot")
    private int arrivalLot;

    /** 仕入数. */
    @Column(name = "purchase_lot")
    private int purchaseLot;

    /** 返品数量. */
    @Column(name = "return_lot")
    private int returnLot;

    /** 純仕入数. */
    @Column(name = "net_purchase_lot")
    private int netPurchaseLot;

    /** 純仕入入金額. */
    @Column(name = "net_purchase_deposit_amount")
    private int netPurchaseDepositAmount;

    /** 発注完了日. */
    @Column(name = "order_complete_at")
    private Date orderCompleteAt;

    /** 月末日（当月）. */
    @Column(name = "month_end_at")
    private Date monthEndAt;

    /** 月末発注数量（当月）. */
    @Column(name = "month_end_order_lot")
    private int monthEndOrderLot;

    /** 月末裁断数量（当月）. */
    @Column(name = "month_end_cut_lot")
    private int monthEndCutLot;

    /** 月末納品依頼数（当月）. */
    @Column(name = "month_end_delivery_lot")
    private int monthEndDeliveryLot;

    /** 月末入荷数量（当月）. */
    @Column(name = "month_end_arrival_lot")
    private int monthEndArrivalLot;

    /** 月末仕入数量（当月）. */
    @Column(name = "month_end_purchase_lot")
    private int monthEndPurchaseLot;

    /** 月末返品数量（当月）. */
    @Column(name = "month_end_return_lot")
    private int monthEndReturnLot;

    /** 月末純仕入数量（当月）. */
    @Column(name = "month_end_net_purchase_lot")
    private int monthEndNetPurchaseLot;

    /** 月末純仕入金額（当月）. */
    @Column(name = "month_end_net_purchase_deposit_amount")
    private int monthEndNetPurchaseDepositAmount;

    /** 発注完了日付（当月）. */
    @Column(name = "month_end_order_complete_at")
    private Date monthEndOrderCompleteAt;

    /** 月末日（前月）. */
    @Column(name = "previous_month_end_at")
    private Date previousMonthEndAt;

    /** 月末発注数量（前月）. */
    @Column(name = "previous_month_end_order_lot")
    private int previousMonthEndOrderLot;

    /** 月末裁断数量（前月）. */
    @Column(name = "previous_month_end_cut_lot")
    private int previousMonthEndCutLot;

    /** 月末納品依頼数（前月）. */
    @Column(name = "previous_month_end_delivery_lot")
    private int previousMonthEndDeliveryLot;

    /** 月末入荷数量（前月）. */
    @Column(name = "previous_month_end_arrival_lot")
    private int previousMonthEndArrivalLot;

    /** 月末仕入数量（前月）. */
    @Column(name = "previous_month_end_purchase_lot")
    private int previousMonthEndPurchaseLot;

    /** 月末返品数量（前月）. */
    @Column(name = "previous_month_end_return_lot")
    private int previousMonthEndReturnLot;

    /** 月末純仕入数量（前月）. */
    @Column(name = "previous_month_end_net_purchase_lot")
    private int previousMonthEndNetPurchaseLot;

    /** 月末純仕入金額（前月）. */
    @Column(name = "previous_month_end_net_purchase_deposit_amount")
    private int previousMonthEndNetPurchaseDepositAmount;

    /** 発注完了日付（前月）. */
    @Column(name = "previous_month_end_order_complete_at")
    private Date previousMonthEndOrderCompleteAt;

    /** 月末日（前々月）. */
    @Column(name = "month_before_end_at")
    private Date monthBeforeEndAt;

    /** 月末発注数量（前々月）. */
    @Column(name = "month_before_end_order_lot")
    private int monthBeforeEndOrderLot;

    /** 月末裁断数量（前々月）. */
    @Column(name = "month_before_end_cut_lot")
    private int monthBeforeEndCutLot;

    /** 月末納品依頼数（前々月）. */
    @Column(name = "month_before_end_delivery_lot")
    private int monthBeforeEndDeliveryLot;

    /** 月末入荷数量（前々月）. */
    @Column(name = "month_before_end_arrival_lot")
    private int monthBeforeEndArrivalLot;

    /** 月末仕入数量（前々月）. */
    @Column(name = "month_before_end_purchase_lot")
    private int monthBeforeEndPurchaseLot;

    /** 月末返品数量（前々月）. */
    @Column(name = "month_before_end_return_lot")
    private int monthBeforeEndReturnLot;

    /** 月末純仕入数量（前々月）. */
    @Column(name = "month_before_end_net_purchase_lot")
    private int monthBeforeEndNetPurchaseLot;

    /** 月末純仕入金額（前々月）. */
    @Column(name = "month_before_end_net_purchase_deposit_amount")
    private int monthBeforeEndNetPurchaseDepositAmount;

    /** 発注完了日付（前々月）. */
    @Column(name = "month_before_end_order_complete_at")
    private Date monthBeforeEndOrderCompleteAt;

    /** 送信区分. */
    @Column(name = "send_code")
    private String sendCode;

    /** 送信日. */
    @Column(name = "send_at")
    private Date sendAt;

    /** 色名称. */
    @Column(name = "color_name")
    private String colorName;

    /** ソート順. */
    @Column(name = "sort_order")
    private String sortOrder;
}
