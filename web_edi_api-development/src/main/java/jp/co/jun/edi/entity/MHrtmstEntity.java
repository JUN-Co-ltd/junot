package jp.co.jun.edi.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 課別配分率マスタのEntity.
 */
@Entity
@Table(name = "m_hrtmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MHrtmstEntity extends GenericEntity {
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

    /** 店舗:配分課. */
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

    //#PRD_0138 #10680 mod JFE start
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

    /** 登録日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;

    /** 登録ユーザID. */
    @CreatedBy
    @Column(name = "created_user_id", updatable = false)
    private BigInteger createdUserId;

    /** 更新日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Date updatedAt;

    /** 更新ユーザID. */
    @LastModifiedBy
    @Column(name = "updated_user_id")
    private BigInteger updatedUserId;

    /** 削除日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
    //#PRD_0138 #10680 mod JFE end
}
