package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.LgSendTypeConverter;
import jp.co.jun.edi.entity.converter.PartNoToBrandCodeConverter;
import jp.co.jun.edi.entity.extended.key.ExtendedTInventoryShipmentSearchResultKey;
import jp.co.jun.edi.type.LgSendType;
import lombok.Data;

/**
 * 在庫出荷一覧検索結果のEntity.
 */
@Entity
@Table(name = "t_inventory_shipment")
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTInventoryShipmentSearchResultEntity  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private ExtendedTInventoryShipmentSearchResultKey wReplenishmentShippingInstructionKey;

    /** 出荷場所. */
    @Column(name = "cargo_place")
    private String cargoPlace;

    /** ブランドコード. */
    @Column(name = "convert_brand_code")
    @Convert(converter = PartNoToBrandCodeConverter.class)
    private String brandCode;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** 品名. */
    @Column(name = "product_name")
    private String productName;

    /** 数量. */
    @Column(name = "delivery_lot_sum")
    private Integer deliveryLotSum;

    /** 上代金額. */
    @Column(name = "retail_price_sum")
    private BigDecimal retailPriceSum;

    /** LG送信区分. */
    @Column(name = "lg_send_type")
    @Convert(converter = LgSendTypeConverter.class)
    private LgSendType lgSendType;
}
