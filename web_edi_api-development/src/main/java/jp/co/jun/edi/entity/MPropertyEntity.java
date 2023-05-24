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

import jp.co.jun.edi.entity.converter.PropertyCategoryTypeConverter;
import jp.co.jun.edi.type.PropertyCategoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * プロパティマスタ.
 *
 */
@Entity
@Table(name = "m_property")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MPropertyEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** カテゴリ. */
    @Convert(converter = PropertyCategoryTypeConverter.class)
    @Column(name = "category")
    private PropertyCategoryType category;

    /** 設定項目名称. */
    @Column(name = "item_name")
    private String itemName;

    /** 設定値. */
    @Column(name = "item_value")
    private String itemValue;

    /** 説明. */
    @Column(name = "description")
    private String description;
}
