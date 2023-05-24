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
 * JAN採番マスタのEntity.
 */
@Entity
@Table(name = "m_jan_number")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MJanNumberEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** JANマスタID. **/
    @Column(name = "jan_id")
    private BigInteger janId;

    /** チェックディジット無しJANコード. **/
    @Column(name = "non_digit_jan_code")
    private String nonDigitJanCode;

    /** チェックデジット. **/
    @Column(name = "check_digit")
    private String checkDigit;

    /** JAN使用済フラグ. **/
    @Column(name = "used_flg")
    private boolean usedFlg;
}
