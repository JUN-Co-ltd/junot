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

import jp.co.jun.edi.entity.converter.FukukitaruMasterDeliveryTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterOrderTypeConverter;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the t_f_item database table.
 *
 */
@Entity
@Table(name = "m_f_input_assist_set")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFInputAssistSetEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** フクキタルデリバリタイプ. */
    @Convert(converter = FukukitaruMasterDeliveryTypeConverter.class)
    @Column(name = "delivery_type")
    private FukukitaruMasterDeliveryType deliveryType;

    /** フクキタル発注種別. */
    @Convert(converter = FukukitaruMasterOrderTypeConverter.class)
    @Column(name = "order_type")
    private FukukitaruMasterOrderType orderType;

    /** セット名. */
    @Column(name = "set_name")
    private String setName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

}
