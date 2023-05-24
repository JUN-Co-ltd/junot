package jp.co.jun.edi.entity.extended;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.AllocationTypeConverter;
import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.entity.converter.ShopKindTypeConverter;
import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.ShopKindType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張発注生産システムの店舗マスタのEntity.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Entity
@Table(name = "m_tnpmst")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedMTnpmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    /** 店舗コード. */
    private String shpcd;

    /** 組織コード:事業部コード. */
    private String groupcd;

    // districtcd   // ディストリクトコード
    // areacd   // エリアコード

    /** 店舗区分. */
    @Convert(converter = ShopKindTypeConverter.class)
    private ShopKindType shopkind;

    /** 店舗名全角. */
    private String name;

    /** 店舗略称名. */
    private String sname;

    // nameh    // 店舗名半角
    // yubin    // 郵便番号

    /** 住所１. */
    private String add1;

    /** 住所２. */
    private String add2;

    /** 住所３. */
    private String add3;

    /** 住所４. */
    private String add4;

    /** 電話番号:建屋代表. */
    private String telban;

    // choktel  // 直通電話

    /** FAX番号:建屋代表. */
    private String faxban;

    // allowtype  // 移動可能区分
    // 24hflg    // 24時間営業可否フラグ
    // disckbn  // デフォルト値割引可否区分
    // dsalkbn  // デフォルト値割引併用可否区分
    // sdiscbn  // セール商品値値引可否区分
    // taxdkbn  // 消費税表示区分
    // taxinc   // 内税端数処理区分
    // taxexc   // 外税端数処理区分
    // shopinfo // 店舗情報
    // commer   // CM内容
    // telnumkey    // 電話番号検索キー
    // accountnum   // 売掛口座
    // numres1  // 数値リザーブ１
    // numres2  // 数値リザーブ２
    // numres3  // 数値リザーブ３
    // numres4  // 数値リザーブ４
    // numres5  // 数値リザーブ５
    // strres1  // 文字列リザーブ１
    // strres2  // 文字列リザーブ２
    // strres3  // 文字列リザーブ３
    // strres4  // 文字列リザーブ４
    // strres5  // 文字列リザーブ５
    // floorspace   // 坪数
    // shoptype // 店舗形態（未使用）
    // managflg // 店舗管理対象フラグ
    // tsfarflg // 移動入荷自動確定フラグ
    // returflg // 物流返品実績取込フラグ
    // importflg    // 売上取込フラグ
    // importfmt    // 売上取込フォーマット
    // stockkind    // 棚卸区分
    // shopfmt  // 店舗形態
    // khshpcd  // 経費店舗コード
    // kencd    // 都道府県コード
    // s1atena  // 商品発送先宛名
    // s1yubin  // 商品発送先郵便番号
    // s1add1   // 商品発送先住所１
    // s1add2   // 商品発送先住所２
    // s1add3   // 商品発送先住所３
    // s1telban // 商品発送先電話番号
    // s1faxban // 商品発送先FAX番号
    // s2atena  // 返品伝票送付先宛名
    // s2yubin  // 返品伝票送付先郵便番号
    // s2add1   // 返品伝票送付先住所１
    // s2add2   // 返品伝票送付先住所２
    // s2add3   // 返品伝票送付先住所３
    // s2telban // 返品伝票送付先電話番号
    // s2faxban // 返品伝票送付先FAX番号
    // s3atena  // セール納品場所宛名
    // s3yubin  // セール納品場所郵便番号
    // s3add1   // セール納品場所住所１
    // s3add2   // セール納品場所住所２
    // s3add3   // セール納品場所住所３
    // s3telban // セール納品場所電話番号
    // s3faxban // セール納品場所FAX番号
    // disbrcd  // 表示ブランド
    // gidflag  // GIDカード取扱
    // coupflg  // お買物利用券取扱
    // allowadd // 許可IPアドレス
    // channel  // チャネル
    // marketkind   // 販路
    // spchgrate    // SP区分切替閾値
    // engrectype   // レシート英語表記区分

    /** 開店日 .*/
    private String opnymd;

    /** 閉店日 .*/
    private String clsymd;

    // opntime  // 開店時刻
    // clstime  // 閉店時刻
    // gyos4    // 丸井店舗区分
    // ktubosu  // 契約坪数
    // jtubosu  // 実坪数
    // jusindat // 店舗消化送信データ
    // haiten   // 廃店
    // yakan    // 夜間金庫
    // syokbn   // 消化売上データ
    // warekind // 倉庫区分
    // decikind // 自動確定区分
    // decitime // 自動確定リードタイム
    // locatype // 引当可否

    /** 配分区分. */
    @Convert(converter = AllocationTypeConverter.class)
    private AllocationType distrikind;

    // secflg   // 課跨ぎフラグ
    // trkei    // 取引形態
    // ndtype   // 納伝区分
    // pricekbnp    // 百貨店プロパープライス区分
    // pricekbns    // 百貨店セールプライス区分
    // glncd    // GLNコード
    // maruicd  // 丸井店舗コード
    // clbflg   // コラボ連携フラグ
    // posflg   // 自社POS有無
    // trcd // 取引会社コード
    // johoflg  // 会員情報公開不可フラグ
    // rireflg  // 購買履歴公開不可フラグ
    // bhinflg  // B品フラグ

    /** 直送フラグ. */
    @Column(name = "directdeliveryflg")
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType directDeliveryFlg;

    /** 削除済区分. */
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType deletedtype;

    // updateflg    // 更新フラグ
    // insdt    // 登録日時
    // upddt    // 更新日時
    // insuserid    // 登録ユーザーID
    // upduserid    // 更新ユーザーID
    // inspgmid // 登録プログラムID
    // updpgmid // 更新プログラムI
    // verid    // バージョンカラム

    /** 配分課. */
    private String hka;

    /** 配分順. */
    private String hjun;
}
