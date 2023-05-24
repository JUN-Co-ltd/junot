package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張フクキタル品番情報のEntity.
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTFItemEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** カテゴリコード（VIS：空文字固定）. */
    @Column(name = "category_code")
    private BigInteger categoryCode;

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
     * シールへの付記用語印字.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_appendices_term")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printAppendicesTerm;

    /**
     * 原産国印字.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_coo")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printCoo;

    /**
     * シールへの品質印字.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_parts")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printParts;

    /**
     * QRコードの有無.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_qrcode")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printQrcode;

    /** 洗濯ネームサイズ印字.
     * false：印字しない(0)、true:印字する(1)
     * */
    @Column(name = "print_size")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printSize;

    /**
     * シールへの絵表示印字.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_wash_pattern")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printWashPattern;

    /** シールへのリサイクルマーク印字. */
    @Column(name = "recycle_mark")
    private BigInteger recycleMark;

    /** REEFUR用ブランド（VIS：空文字固定）. */
    @Column(name = "reefur_private_brand_code")
    private String reefurPrivateBrandCode;

    /** サタデーズサーフ用NY品番（VIS：空文字固定）. */
    @Column(name = "saturdays_private_ny_part_no")
    private String saturdaysPrivateNyPartNo;

    /** アテンションシールのシール種類. */
    @Column(name = "sticker_type_code")
    private BigInteger stickerTypeCode;

    /** 洗濯ネームテープ種類. */
    @Column(name = "tape_code")
    private BigInteger tapeCode;

    /** 洗濯ネームテープ巾. */
    @Column(name = "tape_width_code")
    private BigInteger tapeWidthCode;

    /** 中国内版情報製品分類. */
    @Column(name = "cn_product_category")
    private BigInteger cnProductCategory;

    /** 中国内版情報製品種別. */
    @Column(name = "cn_product_type")
    private BigInteger cnProductType;

    /** 洗濯ネームテープ種類名称. */
    @Column(name = "tape_name")
    private String tapeName;

    /** 洗濯ネームテープ巾名称. */
    @Column(name = "tape_width_name")
    private String tapeWidthName;

    /** シールへのリサイクルマーク印字名称. */
    @Column(name = "recycle_name")
    private String recycleName;

    /** アテンションシールのシール種類名称. */
    @Column(name = "seal_name")
    private String sealName;

    /** 中国内版情報製品分類名称. */
    @Column(name = "product_category_name")
    private String productCategoryName;

    /** 中国内版情報製品種別名称. */
    @Column(name = "product_type_name")
    private String productTypeName;

    /**
     * シールへの絵表示印字.
     * false：印字しない(0)、true:印字する(1)
     */
    @Column(name = "print_sustainable_mark")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType printSustainableMark;
}
