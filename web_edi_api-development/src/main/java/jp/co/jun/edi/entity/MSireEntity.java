package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/**
 * 仕入先マスタ・工場マスタのEntity.
 * 取引先登録画面表示用のEntity.
 */
@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
//@EqualsAndHashCode(callSuper = true)
//public class MSireEntity extends GenericEntity {
public class MSireEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 仕入先／工場／ＳＰＯＴ. */
    private String reckbn;

    /** 仕入先コード. */
    @Column(name = "sire_code")
    private String sireCode;

    /** 仕入先名・正式名. */
    @Column(name = "sire_name")
    private String sireName;

    /** 工場コード. */
    @Column(name = "koj_code")
    private String kojCode;

    /** 工場名・正式名. */
    @Column(name = "koj_name")
    private String kojName;

    /** 工場名・省略名. */
    @Column(name = "skoj_name")
    private String skojName;

    /** 国内／国外. */
    @Column(name = "in_out")
    private String inOut;

    /** 仕入先区分. */
    private String sirkbn;

    /** 管轄仕入先. */
    @Column(name = "knkt_sire")
    private String knktSire;

    /** 郵便番号. */
    private String yubin;

    /** 住所１. */
    private String add1;

    /** 住所２. */
    private String add2;

    /** 住所３. */
    private String add3;

    /** 電話番号. */
    private String tel1;

    /** 有害物質対応区分. */
    private String yugaikbn;

    /** 有害物質対応日付. */
    private String yugaiymd;

    /** 管轄ブランド1. */
    private String brand1;

    /** 管轄ブランド2. */
    private String brand2;

    /** 管轄ブランド3. */
    private String brand3;

    /** 管轄ブランド4. */
    private String brand4;

    /** 管轄ブランド5. */
    private String brand5;

    /** 管轄ブランド6. */
    private String brand6;

    /** 管轄ブランド7. */
    private String brand7;

    /** 管轄ブランド8. */
    private String brand8;

    /** 管轄ブランド9. */
    private String brand9;

    /** 管轄ブランド10. */
    private String brand10;

    /** 管轄ブランド11. */
    private String brand11;

    /** 管轄ブランド12. */
    private String brand12;

    /** 管轄ブランド13. */
    private String brand13;

    /** 管轄ブランド14. */
    private String brand14;

    /** 管轄ブランド15. */
    private String brand15;

    /** 管轄ブランド16. */
    private String brand16;

    /** 管轄ブランド17. */
    private String brand17;

    /** 管轄ブランド18. */
    private String brand18;

    /** 管轄ブランド19. */
    private String brand19;

    /** 管轄ブランド20. */
    private String brand20;

    /** 管轄ブランド21. */
    private String brand21;

    /** 管轄ブランド22. */
    private String brand22;

    /** 管轄ブランド23. */
    private String brand23;

    /** 管轄ブランド24. */
    private String brand24;

    /** 管轄ブランド25. */
    private String brand25;

    /** 管轄ブランド26. */
    private String brand26;

    /** 管轄ブランド27. */
    private String brand27;

    /** 管轄ブランド28. */
    private String brand28;

    /** 管轄ブランド29. */
    private String brand29;

    /** 管轄ブランド30. */
    private String brand30;

    /** 生地メーカー. */
    private String hkiji;

    /** 製品／縫製メーカー. */
    private String hseihin;

    /** 値札発注先. */
    private String hnefuda;

    /** 附属品メーカー. */
    private String hfuzoku;

    /** 発注書・送付方法. */
    private String hsofkbn;

    /** 発注書・メールアドレス. */
    private String hemail1;

    /** 納品依頼・送付方法. */
    private String nsofkbn;

    /** 納品依頼・メールアドレス. */
    private String nemail1;

    /** 受領書・送付方法. */
    private String ysofkbn;

    /** 受領書・メールアドレス. */
    private String yemail1;
}
