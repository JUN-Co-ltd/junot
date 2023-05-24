package jp.co.jun.edi.entity;

import java.math.BigDecimal;
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
 * 店舗別配分率マスタのEntity.
 */
@Entity
@Table(name = "m_store_hrtmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MStoreHrtmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ブランド. */
    @Column(name = "brand")
    private String brandCode;

    /** アイテム. */
    @Column(name = "item")
    private String itemCode;

    /** シーズン. */
    @Column(name = "season")
    private String season;

    /** 配分率区分. */
    @Column(name = "hrtkbn")
    private String hrtkbn;

    /** 店舗. */
    @Column(name = "shpcd")
    private String shpcd;

    /** 配分率名. */
    @Column(name = "rtname")
    private String rtname;

    /** 配分率. */
    @Column(name = "hritu")
    private BigDecimal hritu;

    /** メンテ区分. */
    @Column(name = "mntflg")
    private String mntflg;

    // 取得する値を減らすため以下のカラムは設定しない
    // /** 登録日. */
    // @Column(name = "crtymd")
    // private String crtymd;
    //
    // /** 修正日. */
    // @Column(name = "updymd")
    // private String updymd;
    //
    // /** 入力者. */
    // @Column(name = "tanto")
    // private String tanto;
    //
    // /** 送信区分. */
    // @Column(name = "souflg")
    // private String souflg;
    //
    // /** 送信日. */
    // @Column(name = "souymd")
    // private String souymd;
}
