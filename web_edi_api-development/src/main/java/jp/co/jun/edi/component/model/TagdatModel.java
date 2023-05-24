package jp.co.jun.edi.component.model;

import java.io.Serializable;

import lombok.Data;

/**
 * TAGDAT作成用Model.
 */
@Data
public class TagdatModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 作成日. */
    private String crtymd;

    /** ブランド. */
    private String brkg;

    /** SEQ. */
    private String seq;

    /** 年度. */
    private String datrec;

    /** シーズン. */
    private String season;

    /** 品番・品種. */
    private String hskg;

    /** 品番・通番. */
    private String tuban;

    /** 上代. */
    private String jodai;

    /** 発注番号. */
    private String hacno;

    /** 引取回数. */
    private String nkai;

    /** カラー. */
    private String iro;

    /** サイズコード. */
    private String size;

    /** サイズ記号. */
    private String szkg;

    /** ＮＷ７. */
    private String nw7;

    /** ＪＡＮコード. */
    private String jan;

    /** 税込上代. */
    private String zjodai;

    /** 予備. */
    private String yobi;

    /** 作成時間. */
    private String crthms;

    /** 送信種別. */
    private String syubt;
}
