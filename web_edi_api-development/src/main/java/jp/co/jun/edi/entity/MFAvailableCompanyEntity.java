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
 * フクキタル用利用可能なメーカーとブランドの管理マスタ情報.
 *
 */
@Entity
@Table(name = "m_f_available_company")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MFAvailableCompanyEntity extends GenericEntity {
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

    /** 有効(true)/無効(false). */
    @Column(name = "enabled")
    private boolean enabled;

}
