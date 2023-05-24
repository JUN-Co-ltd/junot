package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * TAGDAT情報Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTagdatEntity implements Serializable {
    private static final long serialVersionUID = 1L;

	// PRD_0149 mod JFE start
    /** ID. */
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
    //private BigInteger id;
	private String id;
	// PRD_0149 mod JFE end

    /** 作成日. */
    @Column(name = "crtymd")
    private String crtymd;

    /** ブランド. */
    @Column(name = "brkg")
    private String brkg;

    /** 年度. */
    @Column(name = "datrec")
    private Integer datrec;

    /** シーズン. */
    @Column(name = "season")
    private String season;

    /** 品番. */
    @Column(name = "partno")
    private String partNo;

    /** 上代. */
    @Column(name = "jodai")
    private BigDecimal jodai;

    /** 発注番号. */
    @Column(name = "hacno")
    private BigInteger hacno;

    /** カラー. */
    @Column(name = "iro")
    private String iro;

    /** サイズ記号. */
    @Column(name = "szkg")
    private String szkg;

    /** ＪＡＮコード. */
    @Column(name = "jan")
    private String jan;

    /** 軽減税率対象フラグ. */
    @Column(name = "taxflg")
    private Integer taxflg;

    /** 作成時間. */
    @Column(name = "crthms")
    private String crthms;

    /** 送信種別. */
    @Column(name = "syubt")
    private String syubt;

}
