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
 * JANマスタのEntity.
 */
@Entity
@Table(name = "m_jan")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MJanEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 国コード. **/
    @Column(name = "country_code")
    private int countryCode;

    /** 事業者コード. **/
    @Column(name = "business_code")
    private int businessCode;

    /** 連番. **/
    @Column(name = "sequence_number")
    private int sequenceNumber;

    /** 最小値. **/
    @Column(name = "min_number")
    private int minNumber;

    /** 最大値. **/
    @Column(name = "max_number")
    private int maxNumber;

    /** 増分値. **/
    @Column(name = "increment_value")
    private int incrementValue;

    /** JAN採番済フラグ. **/
    @Column(name = "jan_numbered_flg")
    private boolean janNumberedFlg;

}
