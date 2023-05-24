package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * フクキタル用中国内販情報製品種別マスタ.
 *
 */
@Entity
@Table(name = "m_f_cn_product_type")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFCnProductTypeEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** アテンションタグコード. */
    @Column(name = "product_type_code")
    private String productTypeCode;

    /** ブランドコード. */
    @Column(name = "product_type_name")
    private String productTypeName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

}
