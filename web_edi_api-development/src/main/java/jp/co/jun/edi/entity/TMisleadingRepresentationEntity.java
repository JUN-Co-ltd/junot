package jp.co.jun.edi.entity;

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

import jp.co.jun.edi.entity.converter.MisleadingRepresentationTypeConverter;
import jp.co.jun.edi.type.MisleadingRepresentationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 優良誤認検査情報のEntity.
 */
@Entity
@Table(name = "t_misleading_representation")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TMisleadingRepresentationEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** 優良誤認検査対象区分. */
    @Column(name = "misleading_representation_type")
    @Convert(converter = MisleadingRepresentationTypeConverter.class)
    private MisleadingRepresentationType misleadingRepresentationType;

    /** 色. */
    @Column(name = "color_code")
    private String colorCode;

    /** 原産国. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 生産メーカー. */
    @Column(name = "mdf_maker_code")
    private String mdfMakerCode;

    /** 承認日. */
    @Column(name = "approval_at")
    private Date approvalAt;

    /** 承認者アカウント名. */
    @Column(name = "approval_user_account_name")
    private String approvalUserAccountName;

    /** メモ. */
    @Column(name = "memo")
    private String memo;

}
