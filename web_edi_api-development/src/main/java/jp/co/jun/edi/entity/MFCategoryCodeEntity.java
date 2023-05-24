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
 * The persistent class for the m_f_category_code database table.
 *
 */
@Entity
@Table(name = "m_f_category_code")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFCategoryCodeEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** カテゴコード. */
    @Column(name = "category_code")
    private String categoryCode;

    /** カテゴリコード名. */
    @Column(name = "category_name")
    private String categoryName;

    /** 並び順. */
    @Column(name = "sort_order")
    private BigInteger sortOrder;
}
