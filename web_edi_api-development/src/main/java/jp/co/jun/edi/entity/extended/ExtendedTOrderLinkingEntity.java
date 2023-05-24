package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 拡張発注情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTOrderLinkingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 生産メーカー  . */
    @Column(name = "sire")
    private String sire;

}
