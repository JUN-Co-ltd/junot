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
 * TAGDATのEntity.
 */
@Entity
//PRD_0184 mod JFE start
//@Table(name = "tagdat")
@Table(name = "t_tagdat")
//PRD_0184 mod JFE end
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TagdatEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 作成日. */
    @Column(name = "crtymd")
    private String crtymd;

    /** ブランド. */
    @Column(name = "brkg")
    private String brkg;

    /** SEQ. */
    @Column(name = "seq")
    private String seq;

    /** 年度. */
    @Column(name = "datrec")
    private String datrec;

    /** シーズン. */
    @Column(name = "season")
    private String season;

    /** 品番・品種. */
    @Column(name = "hskg")
    private String hskg;

    /** 品番・通番. */
    @Column(name = "tuban")
    private String tuban;

    /** 上代. */
    @Column(name = "jodai")
    private String jodai;

    /** 発注番号. */
    @Column(name = "hacno")
    private String hacno;

    /** 引取回数. */
    @Column(name = "nkai")
    private String nkai;

    /** カラー. */
    @Column(name = "iro")
    private String iro;

    /** サイズコード. */
    @Column(name = "size")
    private String size;

    /** サイズ記号. */
    @Column(name = "szkg")
    private String szkg;

    /** ＮＷ７. */
    @Column(name = "nw7")
    private String nw7;

    /** ＪＡＮコード. */
    @Column(name = "jan")
    private String jan;

    /** 税込上代. */
    @Column(name = "zjodai")
    private String zjodai;

    /** 予備. */
    @Column(name = "yobi")
    private String yobi;

    /** 作成時間. */
    @Column(name = "crthms")
    private String crthms;

    /** 送信種別. */
    @Column(name = "syubt")
    private String syubt;

    /** 送信ステータス. */
    @Column(name = "send_status")
    private int sendStatus;

    /** 登録ユーザID. */
    @Column(name = "created_user_id")
    private BigInteger createdUserId;

    /** 更新ユーザID. */
    @Column(name = "updated_user_id")
    private BigInteger updatedUserId;
}
