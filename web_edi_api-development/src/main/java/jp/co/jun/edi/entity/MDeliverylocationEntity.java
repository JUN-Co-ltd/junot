// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
//import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 納入場所マスタ情報のEntity.
 */
@Entity
//@Table(name = "m_delivery_location")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MDeliverylocationEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 場所名称. */
    @Column(name = "company_name")
    private String companyName;

    /** TCフラグ. */
    @Column(name = "tc_flg")
    private boolean tcFlg;

}
// PRD_0123 #7054 add JFE end