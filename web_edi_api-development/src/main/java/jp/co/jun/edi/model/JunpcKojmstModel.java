package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムの工場マスタ.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcKojmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    //    private String  sire;
    private String kojcd;
    //    private String  reckbn;
    //    private String  sirkbn;
    //    private String  sname;
    private String name;
    //    private String  yubin;
    //    private String  add1;
    //    private String  add2;
    //    private String  add3;
    //    private String  tel1;
    //    private String  fax1;
    //    private String  hsofkbn;
    //    private String  hfax;
    //    private String  hemail1;
    //    private String  nsofkbn;
    //    private String  nfax;
    //    private String  nemail1;
    //    private String  ysofkbn;
    //    private String  yfax;
    //    private String  yemail1;
    //    private String  hkiji;
    //    private String  hseihin;
    //    private String  hnefuda;
    //    private String  hfuzoku;
    //    private String  brand1;
    //    private String  brand2;
    //    private String  brand3;
    //    private String  brand4;
    //    private String  brand5;
    //    private String  brand6;
    //    private String  brand7;
    //    private String  brand8;
    //    private String  brand9;
    //    private String  brand10;
    //    private String  brand11;
    //    private String  brand12;
    //    private String  brand13;
    //    private String  brand14;
    //    private String  brand15;
    //    private String  brand16;
    //    private String  brand17;
    //    private String  brand18;
    //    private String  brand19;
    //    private String  brand20;
    //    private String  brand21;
    //    private String  brand22;
    //    private String  brand23;
    //    private String  brand24;
    //    private String  brand25;
    //    private String  brand26;
    //    private String  brand27;
    //    private String  brand28;
    //    private String  brand29;
    //    private String  brand30;
    //    private String  sdenflg;
    //    private String  souflg;
    //    private String  mntflg;
}
