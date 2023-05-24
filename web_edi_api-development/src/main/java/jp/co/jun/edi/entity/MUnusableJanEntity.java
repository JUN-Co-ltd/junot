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
 * 使用不可マスタのEntity.
 */
@Entity
@Table(name = "m_unusable_jan")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MUnusableJanEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** JANコード. **/
    @Column(name = "jan_code")
    private String janCode;

    /** 品番. **/
    @Column(name = "part_no")
    private String partNo;
}
