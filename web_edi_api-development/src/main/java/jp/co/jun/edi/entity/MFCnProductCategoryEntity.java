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
 * フクキタル用中国内販情報製品分類マスタ.
 *
 */
@Entity
@Table(name = "m_f_cn_product_category")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFCnProductCategoryEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** アテンションタグコード. */
    @Column(name = "product_category_code")
    private String productCategoryCode;

    /** ブランドコード. */
    @Column(name = "product_category_name")
    private String productCategoryName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;

}
