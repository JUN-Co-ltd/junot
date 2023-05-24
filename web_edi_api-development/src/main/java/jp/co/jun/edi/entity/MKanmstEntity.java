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
 * 管理マスタのEntity.
 */
@Entity
@Table(name = "m_kanmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MKanmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 管理日付区分. */
    @Column(name = "kankbn")
    private String kankbn;

    /** 日計日. */
    @Column(name = "nitymd")
    private String nitymd;

    /** 休日フラグ. */
    @Column(name = "kyuflg")
    private String kyuflg;

    /** 翌営業日. */
    @Column(name = "yegymd")
    private String yegymd;

    /** 月度締日. */
    @Column(name = "simymd")
    private String simymd;

    /** 棚卸日. */
    @Column(name = "tnaymd")
    private String tnaymd;

    /** 月次預け反処理日. */
    @Column(name = "azkrun")
    private String azkrun;

    /** 預け反設定日. */
    @Column(name = "azkset")
    private String azkset;

    /** その他日付１. */
    @Column(name = "etcymd1")
    private String etcymd1;

    /** その他日付２. */
    @Column(name = "etcymd2")
    private String etcymd2;

    /** 月次処理実行日. */
    @Column(name = "runymd")
    private String runymd;

    /** 期首日付. */
    @Column(name = "kisymd")
    private String kisymd;

    /** 期末日付. */
    @Column(name = "kimymd")
    private String kimymd;

    /** 作業日. */
    @Column(name = "wrkymd")
    private String wrkymd;

    /** 確定日. */
    @Column(name = "kakymd")
    private String kakymd;

    /** 消費税率％. */
    @Column(name = "tax")
    private double tax;

    /** 税率改正日. */
    @Column(name = "taxymd")
    private String taxymd;

    /** 改正後税率％. */
    @Column(name = "tax2")
    private double tax2;

    /** メンテ区分. */
    @Column(name = "mntflg")
    private String mntflg;

    /** 登録日. */
    @Column(name = "crtymd")
    private String crtymd;

    /** 修正日. */
    @Column(name = "updymd")
    private String updymd;

    /** 入力者. */
    @Column(name = "tanto")
    private String tanto;

    /** 送信区分. */
    @Column(name = "souflg")
    private String souflg;

    /** 送信日. */
    @Column(name = "souymd")
    private String souymd;
}
