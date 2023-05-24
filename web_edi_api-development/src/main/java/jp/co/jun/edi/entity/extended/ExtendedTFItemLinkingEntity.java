package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 拡張フクキタル品番情報のEntity.
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedTFItemLinkingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** REEFUR用ブランド（VIS：空文字固定）. */
    @Column(name = "reefur_private_brand_code")
    private String reefurPrivateBrandCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** シーズン. */
    @Column(name = "season_code")
    private String seasonCode;

    /** カテゴリコード（VIS：空文字固定）. */
    @Column(name = "category_code")
    private String categoryCode;

    /** サタデーズサーフ用NY品番（VIS：空文字固定）. */
    @Column(name = "saturdays_private_ny_part_no")
    private String saturdaysPrivateNyPartNo;

    /** 原産国名. */
    @Column(name = "coo_name")
    private String cooName;

    /** 上代. */
    @Column(name = "retail_price")
    private BigDecimal retailPrice;

    /** アイテム名. */
    @Column(name = "item_name")
    private BigDecimal itemName;

    /** アイテム種類. */
    @Column(name = "item_type")
    private BigDecimal itemType;

    /** 洗濯ネームテープ種類名称. */
    @Column(name = "tape_name")
    private String tapeName;

    /** 洗濯ネームテープ巾名称. */
    @Column(name = "tape_width_name")
    private String tapeWidthName;

    /** 洗濯ネームサイズ印字. */
    @Column(name = "print_size")
    private String printSize;

    /** NERGY用メリット下札コード1（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code1")
    private String nergyBillCode1;

    /** NERGY用メリット下札コード2（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code2")
    private String nergyBillCode2;

    /** NERGY用メリット下札コード3（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code3")
    private String nergyBillCode3;

    /** NERGY用メリット下札コード4（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code4")
    private String nergyBillCode4;

    /** NERGY用メリット下札コード5（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code5")
    private String nergyBillCode5;

    /** NERGY用メリット下札コード6（VIS：空文字固定）. */
    @Column(name = "nergy_bill_code6")
    private String nergyBillCode6;

    /**
     * QRコードの有無.
     */
    @Column(name = "print_qrcode")
    private String printQrcode;

    /**
     * シールへの絵表示印字.
     */
    @Column(name = "print_wash_pattern")
    private String printWashPattern;

    /**
     * シールへの付記用語印字.
     */
    @Column(name = "print_appendices_term")
    private String printAppendicesTerm;

    /**
     * シールへの品質印字.
     */
    @Column(name = "print_parts")
    private String printParts;

    /** シールへのリサイクルマーク印字. */
    @Column(name = "recycle_code")
    private String recycleCode;

    /** シールコード. */
    @Column(name = "seal_code")
    private String sealCode;

    /** 製品分類. */
    @Column(name = "cn_product_category_code")
    private String cnProductCategoryCode;

    /** 製品種別. */
    @Column(name = "cn_product_type_code")
    private String cnProductTypeCode;

    /** 産地（原産地）. */
    @Column(name = "cn_coo_code")
    private String cnCooCode;

    /** サスティナブルマーク印字. */
    @Column(name = "print_sustainable_mark")
    private String printSustainableMark;

}
