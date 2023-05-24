package jp.co.jun.edi.entity;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.ShippingInstructionDataTypeConverter;
import jp.co.jun.edi.entity.key.WReplenishmentShippingInstructionKey;
import jp.co.jun.edi.type.ShippingInstructionDataType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 補充出荷指示ワーク情報のEntity.
 */
@Entity
@Table(name = "w_replenishment_shipping_instruction")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class WReplenishmentShippingInstructionEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private WReplenishmentShippingInstructionKey wReplenishmentShippingInstructionKey;

    /** 出荷先店舗コード. */
    @Column(name = "shipment_shop_code")
    private String shipmentShopCode;

    /** 保留先店舗コード. */
    @Column(name = "hold_shop_code")
    private String holdShopCode;

    /** 出荷日. */
    @Column(name = "cargo_at")
    private String cargoAt;

    /** 出荷場所. */
    @Column(name = "cargo_place")
    private String cargoPlace;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** カラー. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    @Column(name = "size")
    private String size;

    /** 出荷指示数. */
    @Column(name = "shipping_instruction_lot")
    private Integer shippingInstructionsLot;

    /** データ種別:0：出荷指示　1：確保店舗移動. */
    @Convert(converter = ShippingInstructionDataTypeConverter.class)
    @Column(name = "data_type")
    private ShippingInstructionDataType dataType;

    /** 入力者. */
    @Column(name = "tanto")
    private String tanto;
}
