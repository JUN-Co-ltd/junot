package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * フクキタル連携発注情報Entity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedFukukitaruLinkingPartNoInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** オーダー識別コード. */
    @Column(name = "order_code")
    private String orderCode;

    /** ブランド名. */
    @Column(name = "brand_name")
    private String brandName;

    /** ブランド名記号. */
    @Column(name = "brand_code")
    private String brandCode;

    /** 製品品番. */
    @Column(name = "part_no")
    private String partNo;

    /** 季別. */
    /** 原産国. */
    @Column(name = "coo_code")
    private String cooCode;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** カラー. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズ. */
    /** 洗濯ネームテープ種類. */
    /** 洗濯ネームテープ巾. */
    /** 洗濯ネームサイズ印字. */
    /** 絵表示. */
    /** 洗濯ネーム用付記用語1. */
    /** 洗濯ネーム用付記用語2. */
    /** 洗濯ネーム用付記用語3. */
    /** 洗濯ネーム用付記用語4. */
    /** 洗濯ネーム用付記用語5. */
    /** 洗濯ネーム用付記用語6. */
    /** 洗濯ネーム用付記用語7. */
    /** 洗濯ネーム用付記用語8. */
    /** 洗濯ネーム用付記用語9. */
    /** 洗濯ネーム用付記用語10. */
    /** 洗濯ネーム用付記用語11. */
    /** 洗濯ネーム用付記用語12. */
    /** アテンションタグ用付記用語1. */
    /** アテンションタグ用付記用語2. */
    /** アテンションタグ用付記用語3. */
    /** アテンションタグ用付記用語4. */
    /** アテンションタグ用付記用語5. */
    /** アテンションタグ用付記用語6. */
    /** アテンションタグ用付記用語7. */
    /** アテンションタグ用付記用語8. */
    /** アテンションタグ用付記用語9. */
    /** アテンションタグ用付記用語10. */
    /** アテンションタグ用付記用語11. */
    /** アテンションタグ用付記用語12. */
    /** 部位1. */
    /** 素材1. */
    /** 混率1. */
    /** 部位2. */
    /** 素材2. */
    /** 混率2. */
    /** 部位3. */
    /** 素材3. */
    /** 混率3. */
    /** 部位4. */
    /** 素材4. */
    /** 混率4. */
    /** 部位5. */
    /** 素材5. */
    /** 混率5. */
    /** 部位6. */
    /** 素材6. */
    /** 混率6. */
    /** 部位7. */
    /** 素材7. */
    /** 混率7. */
    /** 部位8. */
    /** 素材8. */
    /** 混率8. */
    /** 部位9. */
    /** 素材9. */
    /** 混率9. */
    /** 部位10. */
    /** 素材10. */
    /** 混率10. */
    /** 部位11. */
    /** 素材11. */
    /** 混率11. */
    /** 部位12. */
    /** 素材12. */
    /** 混率12. */
    /** 部位13. */
    /** 素材13. */
    /** 混率13. */
    /** 部位14. */
    /** 素材14. */
    /** 混率14. */
    /** 部位15. */
    /** 素材15. */
    /** 混率15. */
    /** 部位16. */
    /** 素材16. */
    /** 混率16. */
    /** 部位17. */
    /** 素材17. */
    /** 混率17. */
    /** 部位18. */
    /** 素材18. */
    /** 混率18. */
    /** 部位19. */
    /** 素材19. */
    /** 混率19. */
    /** 部位20. */
    /** 素材20. */
    /** 混率20. */
    /** NERGY用メリット下札コード2. */
    /** NERGY用メリット下札コード3. */
    /** NERGY用メリット下札コード4. */
    /** NERGY用メリット下札コード5. */
    /** NERGY用メリット下札コード6. */
    /** QRコードの有無. */
    /** シールへの絵表示印字. */
    /** シールへの付記用語印字. */
    /** シールへの品質印字. */
    /** シールへのリサイクルマーク印字. */
    /** アテンションシールのシール種類. */
    /** 製品分類. */
    /** 製品種別. */


    /** 発注日. */
    @Temporal(TemporalType.DATE)
    @Column(name = "order_at")
    private Date orderAt;

    /** 発注者コード. */
    /** 請求先コード. */
    /** 納入先コード. */
    /** 納入先担当者. */
    /** 緊急. */
    /** 希望出荷日. */
    /** 契約No.. */
    /** 特記事項. */
    /** 手配先. */
    /** リピート数. */
    /** 工場No.. */
    /** 製品品番. */
    /** 資材コード. */
    /** 数量. */
}
