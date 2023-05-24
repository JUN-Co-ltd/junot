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
import jp.co.jun.edi.entity.converter.FukukitaruMasterTypeConverter;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル用ファイル情報のEntity.
 */
@Entity
@Table(name = "t_f_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TFFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** デリバリ種別. */
    @Convert(converter = FukukitaruMasterDeliveryTypeConverter.class)
    @Column(name = "delivery_type")
    private FukukitaruMasterDeliveryType deliveryType;

    /** マスタ種別. */
    @Convert(converter = FukukitaruMasterTypeConverter.class)
    @Column(name = "master_type")
    private FukukitaruMasterType masterType;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

}
