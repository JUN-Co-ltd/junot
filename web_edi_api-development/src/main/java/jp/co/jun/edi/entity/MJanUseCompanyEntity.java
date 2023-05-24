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
 * JAN使用会社マスタ.
 */
@Entity
@Table(name = "m_jan_use_company")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MJanUseCompanyEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** JANマスタID. **/
    @Column(name = "jan_id")
    private BigInteger janId;

    /** 会社コード. **/
    @Column(name = "company_code")
    private String companyCode;

}
