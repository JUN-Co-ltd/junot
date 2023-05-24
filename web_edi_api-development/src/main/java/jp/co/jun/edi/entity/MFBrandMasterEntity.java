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

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterDeliveryTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterOrderTypeConverter;
import jp.co.jun.edi.entity.converter.FukukitaruMasterTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.FukukitaruMasterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル用ブランド情報.
 *
 */
@Entity
@Table(name = "m_f_brand_master")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFBrandMasterEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** アイテムコード. */
    @Column(name = "item_code")
    private String itemCode;

    /** フクキタルデリバリ種別. */
    @Convert(converter = FukukitaruMasterDeliveryTypeConverter.class)
    @Column(name = "delivery_type")
    private FukukitaruMasterDeliveryType deliveryType;

    /** フクキタル発注種別. */
    @Convert(converter = FukukitaruMasterOrderTypeConverter.class)
    @Column(name = "order_type")
    private FukukitaruMasterOrderType orderType;

    /** フクキタルマスタ種別. */
    @Convert(converter = FukukitaruMasterTypeConverter.class)
    @Column(name = "master_type")
    private FukukitaruMasterType masterType;

    /** マスタIDリスト. */
    @Column(name = "master_id_list")
    private String masterIdList;

    /** 表示フラグ. */
    @Column(name = "display_flg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType displayFlg;

}
