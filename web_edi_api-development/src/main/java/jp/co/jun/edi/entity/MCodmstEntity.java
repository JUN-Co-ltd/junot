package jp.co.jun.edi.entity;

import java.io.Serializable;
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

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 発注生産システムのコードマスタのEntity.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Entity
@Table(name = "m_codmst")
@EntityListeners(AuditingEntityListener.class)
@Data
public class MCodmstEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    private String tblid;
    private String code1;
    private String code2;
    private String code3;
    //    private String code4;
    private String code5;
    //    private String code6;
    //    private String code7;
    //    private String code8;
    private String item1;
    private String item2;
    private String item3;
    private String item4;
    private String item5;
    private String item6;
    private String item7;
    //    private String item8;
    //    private String item9;
    private String item10;
    //    private String item11;
    //    private String item12;
    //    private String item13;
    //    private String item14;
    //    private String item15;
    //    private String item16;
    //    private String item17;
    //    private String item18;
    //    private String item19;
    //    private String item20;
    //    private String item21;
    //    private String item22;
    //    private String item23;
    //    private String item24;
    //    private String item25;
    //    private String item26;
    //    private String item27;
    //    private String item28;
    //    private String item29;
    private String item30;
    private String mntflg;
    //    private String crtymd;
    //    private String updymd;
    //    private String tanto;
    /** 削除日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
}
