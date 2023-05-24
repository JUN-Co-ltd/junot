package jp.co.jun.edi.entity.master;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/**
 * 取引先情報のEntity.
 */
@Entity
@Data
public class SireEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 区分. */
    private String reckbn;

    /** 仕入先コード */
    @Column(name = "sire_code")
    private String sireCode;

    /** 仕入先名 */
    @Column(name = "sire_name")
    private String sireName;

    /** 工場コード */
    @Column(name = "koj_code")
    private String kojCode;

    /** 工場名 */
    @Column(name = "koj_name")
    private String kojName;

    /** 発注区分（生地）. */
    private String hkiji;

    /** 発注区分（製品）. */
    private String hseihin;

    /** 発注区分（値札）. */
    private String hnefuda;

    /** 発注区分（附属）. */
    private String hfuzoku;

    /** ブランドコード. */
    @Column(name = "brand_code")
    private String brandCode;

    /** 発注書送付先区分. */
    private String hsofkbn;

    /** 納品依頼書送付先区分. */
    private String nsofkbn;

    /** 予備受領書送付先区分. */
    private String ysofkbn;

    /** 有害物質対応区分. */
    private String yugaikbn;
}
