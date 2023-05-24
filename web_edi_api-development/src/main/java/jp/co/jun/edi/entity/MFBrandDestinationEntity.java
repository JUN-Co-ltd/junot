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

import jp.co.jun.edi.entity.converter.FukukitaruMasterAddressTypeConverter;
import jp.co.jun.edi.type.FukukitaruMasterDestinationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル用ブランドコード別宛先.
 *
 */
@Entity
@Table(name = "m_f_brand_destination")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFBrandDestinationEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** メーカーコード. */
    @Column(name = "company")
    private String company;

    /** 宛先種別. */
    @Convert(converter = FukukitaruMasterAddressTypeConverter.class)
    @Column(name = "destination_type")
    private FukukitaruMasterDestinationType destinationType;


    /** 会社ID. */
    @Column(name = "destination_id_list")
    private String destinationIdList;

}
