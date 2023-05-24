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
 * 発注生産システムの仕入先マスタのEntity.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Entity
@Table(name = "m_sirmst")
@EntityListeners(AuditingEntityListener.class)
@Data
//PRD_0141 #10656 add start
@EqualsAndHashCode(callSuper = true)
//PRD_0141 #10656 add end
//PRD_0141 #10656 upd start
//public class MSirmstEntity implements Serializable {
public class MSirmstEntity extends GenericEntity {
//PRD_0141 #10656 upd end
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String sire;
    private String sirkbn;
    private String sname;
    private String name;
    private String yubin; //PRD_0141 #10656 JFE コメント解除
    private String add1; //PRD_0141 #10656 JFE コメント解除
    private String add2; //PRD_0141 #10656 JFE コメント解除
    private String add3; //PRD_0141 #10656 JFE コメント解除
    private String tel1; //PRD_0141 #10656 JFE コメント解除
    private String fax1; //PRD_0141 #10656 JFE コメント解除
    private String bank; //PRD_0141 #10656 JFE コメント解除
    private String siten; //PRD_0141 #10656 JFE コメント解除
    private String kozsyu; //PRD_0141 #10656 JFE コメント解除
    private String kozno; //PRD_0141 #10656 JFE コメント解除
    private String meigij; //PRD_0141 #10656 JFE コメント解除
    private String meigik; //PRD_0141 #10656 JFE コメント解除
    private String city; //PRD_0141 #10656 JFE コメント解除
    private String dummy1; //PRD_0141 #10656 JFE コメント解除
    private String brand1; //PRD_0141 #10656 JFE コメント解除
    private String himok1; //PRD_0141 #10656 JFE コメント解除
    private String brand2; //PRD_0141 #10656 JFE コメント解除
    private String himok2; //PRD_0141 #10656 JFE コメント解除
    private String brand3; //PRD_0141 #10656 JFE コメント解除
    private String himok3; //PRD_0141 #10656 JFE コメント解除
    private String brand4; //PRD_0141 #10656 JFE コメント解除
    private String himok4; //PRD_0141 #10656 JFE コメント解除
    private String brand5; //PRD_0141 #10656 JFE コメント解除
    private String himok5; //PRD_0141 #10656 JFE コメント解除
    private String brand6; //PRD_0141 #10656 JFE コメント解除
    private String himok6; //PRD_0141 #10656 JFE コメント解除
    private String brand7; //PRD_0141 #10656 JFE コメント解除
    private String himok7; //PRD_0141 #10656 JFE コメント解除
    private String brand8; //PRD_0141 #10656 JFE コメント解除
    private String himok8; //PRD_0141 #10656 JFE コメント解除
    private String brand9; //PRD_0141 #10656 JFE コメント解除
    private String himok9; //PRD_0141 #10656 JFE コメント解除
    private String brand10; //PRD_0141 #10656 JFE コメント解除
    private String himok10; //PRD_0141 #10656 JFE コメント解除
    private String brand11; //PRD_0141 #10656 JFE コメント解除
    private String himok11; //PRD_0141 #10656 JFE コメント解除
    private String brand12; //PRD_0141 #10656 JFE コメント解除
    private String himok12; //PRD_0141 #10656 JFE コメント解除
    private String brand13; //PRD_0141 #10656 JFE コメント解除
    private String himok13; //PRD_0141 #10656 JFE コメント解除
    private String brand14; //PRD_0141 #10656 JFE コメント解除
    private String himok14; //PRD_0141 #10656 JFE コメント解除
    private String brand15; //PRD_0141 #10656 JFE コメント解除
    private String himok15; //PRD_0141 #10656 JFE コメント解除
    private String brand16; //PRD_0141 #10656 JFE コメント解除
    private String himok16; //PRD_0141 #10656 JFE コメント解除
    private String brand17; //PRD_0141 #10656 JFE コメント解除
    private String himok17; //PRD_0141 #10656 JFE コメント解除
    private String brand18; //PRD_0141 #10656 JFE コメント解除
    private String himok18; //PRD_0141 #10656 JFE コメント解除
    private String brand19; //PRD_0141 #10656 JFE コメント解除
    private String himok19; //PRD_0141 #10656 JFE コメント解除
    private String brand20; //PRD_0141 #10656 JFE コメント解除
    private String himok20; //PRD_0141 #10656 JFE コメント解除
    private String brand21; //PRD_0141 #10656 JFE コメント解除
    private String himok21; //PRD_0141 #10656 JFE コメント解除
    private String brand22; //PRD_0141 #10656 JFE コメント解除
    private String himok22; //PRD_0141 #10656 JFE コメント解除
    private String brand23; //PRD_0141 #10656 JFE コメント解除
    private String himok23; //PRD_0141 #10656 JFE コメント解除
    private String brand24; //PRD_0141 #10656 JFE コメント解除
    private String himok24; //PRD_0141 #10656 JFE コメント解除
    private String brand25; //PRD_0141 #10656 JFE コメント解除
    private String himok25; //PRD_0141 #10656 JFE コメント解除
    private String brand26; //PRD_0141 #10656 JFE コメント解除
    private String himok26; //PRD_0141 #10656 JFE コメント解除
    private String brand27; //PRD_0141 #10656 JFE コメント解除
    private String himok27; //PRD_0141 #10656 JFE コメント解除
    private String brand28; //PRD_0141 #10656 JFE コメント解除
    private String himok28; //PRD_0141 #10656 JFE コメント解除
    private String brand29; //PRD_0141 #10656 JFE コメント解除
    private String himok29; //PRD_0141 #10656 JFE コメント解除
    private String brand30; //PRD_0141 #10656 JFE コメント解除
    private String himok30; //PRD_0141 #10656 JFE コメント解除
    private String ftesury; //PRD_0141 #10656 JFE コメント解除
    private String bubkbn; //PRD_0141 #10656 JFE コメント解除
    private String sofkbn; //PRD_0141 #10656 JFE コメント解除
    private String hkiji; //PRD_0141 #10656 JFE コメント解除
    private String hseihin; //PRD_0112 #7710 JFE コメント解除
    private String hnefuda; //PRD_0141 #10656 JFE コメント解除
    private String hfuzoku; //PRD_0141 #10656 JFE コメント解除
    private String sofhou; //PRD_0141 #10656 JFE コメント解除
    private String dummy2; //PRD_0141 #10656 JFE コメント解除
    private String lokkbn; //PRD_0141 #10656 JFE コメント解除
    private String souflg;
    private String mntflg;
    private String tanto; //PRD_0141 #10656 JFE コメント解除
    private String crtymd; //PRD_0141 #10656 JFE コメント解除
    private String updymd; //PRD_0141 #10656 JFE コメント解除
    private String pgid; //PRD_0141 #10656 JFE コメント解除
    private String souflga; //PRD_0141 #10656 JFE コメント解除
    private String souymda; //PRD_0141 #10656 JFE コメント解除
    private String yugaikbn;
    private String yugaiymd; //PRD_0141 #10656 JFE コメント解除
    /** 削除日時. */
    //@Temporal(TemporalType.TIMESTAMP) //PRD_0141 #10656 JFE コメント化
    //@Column(name = "deleted_at") //PRD_0141 #10656 JFE コメント化
    //private Date deletedAt; //PRD_0141 #10656 JFE コメント化
}
