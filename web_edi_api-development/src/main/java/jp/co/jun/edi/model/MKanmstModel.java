package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 管理マスタのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MKanmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 管理日付区分. */
    private String kankbn;

    /** 日計日. */
    private String nitymd;

    /** 休日フラグ. */
    private String kyuflg;

    /** 翌営業日. */
    private String yegymd;

    /** 月度締日. */
    private String simymd;

    /** 棚卸日. */
    private String tnaymd;

    /** 月次預け反処理日. */
    private String azkrun;

    /** 預け反設定日. */
    private String azkset;

    /** その他日付１. */
    private String etcymd1;

    /** その他日付２. */
    private String etcymd2;

    /** 月次処理実行日. */
    private String runymd;

    /** 期首日付. */
    private String kisymd;

    /** 期末日付. */
    private String kimymd;

    /** 作業日. */
    private String wrkymd;

    /** 確定日. */
    private String kakymd;

    /** 消費税率％. */
    private double tax;

    /** 税率改正日. */
    private String taxymd;

    /** 改正後税率％. */
    private double tax2;

    /** メンテ区分. */
    private String mntflg;

    /** 登録日. */
    private String crtymd;

    /** 修正日. */
    private String updymd;

    /** 入力者. */
    private String tanto;

    /** 送信区分. */
    private String souflg;

    /** 送信日. */
    private String souymd;

}
