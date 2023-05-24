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
import lombok.EqualsAndHashCode;

/**
 * 発注生産システムの工場マスタのEntity.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Entity
@Table(name = "m_kojmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MKojmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    /** 仕入先コード/工場コード／ＳＰＯＴ. */
    private String sire;
    /** 工場コード（スポットコード）. */
    private String kojcd;
    //PRD_0141 #10656 mod JFE start
    private String reckbn;
    //PRD_0141 #10656 mod JFE end
    /** 仕入先区分. */
    private String sirkbn;
    //PRD_0141 #10656 mod JFE start
    private String sname;
    //PRD_0141 #10656 mod JFE end
    /** 仕入先正式名称. */
    private String name;
    //PRD_0141 #10656 mod JFE start
    private String yubin;
    private String add1;
    private String add2;
    private String add3;
    private String tel1;
    private String fax1;
    //PRD_0141 #10656 mod JFE end
    /** 発注書送付先区分. */
    private String hsofkbn;
    //PRD_0141 #10656 mod JFE start
    private String hfax;
    //PRD_0141 #10656 mod JFE end
    /** 発注書送付先メールアドレス１. */
    private String hemail1;
    /** 納品依頼書送付先区分. */
    private String nsofkbn;
    //PRD_0141 #10656 mod JFE start
    @Column(name = "nfax")
    private String nFax;
    //PRD_0141 #10656 mod JFE end
    /** 納品依頼書送付先メールアドレス１. */
    private String nemail1;
    //PRD_0134 #10654 mod JEF start
    //    private String ysofkbn;
    private String ysofkbn;

    //PRD_0134 #10654 mod JEF end
    //    private String yfax;
    //PRD_0134 #10654 mod JEF start
    //    private String yemail1;
    private String yemail1;
    //PRD_0134 #10654 mod JEF end
    //PRD_0141 #10656 mod JFE start
    private String yfax;
    private String hkiji;
    private String hseihin;
    private String hnefuda;
    private String hfuzoku;
    private String brand1;
    private String brand2;
    private String brand3;
    private String brand4;
    private String brand5;
    private String brand6;
    private String brand7;
    private String brand8;
    private String brand9;
    private String brand10;
    private String brand11;
    private String brand12;
    private String brand13;
    private String brand14;
    private String brand15;
    private String brand16;
    private String brand17;
    private String brand18;
    private String brand19;
    private String brand20;
    private String brand21;
    private String brand22;
    private String brand23;
    private String brand24;
    private String brand25;
    private String brand26;
    private String brand27;
    private String brand28;
    private String brand29;
    private String brand30;
    private String sdenflg;
    private String souflg;
    private String mntflg;
    private String tanto;
    private String crtymd;
    private String updymd;
    private String pgid;
    /** 削除日時. */
    //@Temporal(TemporalType.TIMESTAMP)
    //@Column(name = "deleted_at")
    //private Date deletedAt;
    //PRD_0141 #10656 mod JFE end
}
