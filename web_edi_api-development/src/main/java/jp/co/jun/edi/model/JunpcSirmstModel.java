package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 発注生産システムの仕入先マスタ.
 * 検索性能を向上させるため、画面で使用しない項目をコメントアウト化.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcSirmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String sire;
    //    private String sirkbn;
    //    private String sname;
    private String name;
    //    private String yubin;
    //    private String add1;
    //    private String add2;
    //    private String add3;
    //    private String tel1;
    //    private String fax1;
    //    private String bank;
    //    private String siten;
    //    private String kozsyu;
    //    private String kozno;
    //    private String meigij;
    //    private String meigik;
    //    private String city;
    //    private String dummy1;
    //    private String brand1;
    //    private String himok1;
    //    private String brand2;
    //    private String himok2;
    //    private String brand3;
    //    private String himok3;
    //    private String brand4;
    //    private String himok4;
    //    private String brand5;
    //    private String himok5;
    //    private String brand6;
    //    private String himok6;
    //    private String brand7;
    //    private String himok7;
    //    private String brand8;
    //    private String himok8;
    //    private String brand9;
    //    private String himok9;
    //    private String brand10;
    //    private String himok10;
    //    private String brand11;
    //    private String himok11;
    //    private String brand12;
    //    private String himok12;
    //    private String brand13;
    //    private String himok13;
    //    private String brand14;
    //    private String himok14;
    //    private String brand15;
    //    private String himok15;
    //    private String brand16;
    //    private String himok16;
    //    private String brand17;
    //    private String himok17;
    //    private String brand18;
    //    private String himok18;
    //    private String brand19;
    //    private String himok19;
    //    private String brand20;
    //    private String himok20;
    //    private String brand21;
    //    private String himok21;
    //    private String brand22;
    //    private String himok22;
    //    private String brand23;
    //    private String himok23;
    //    private String brand24;
    //    private String himok24;
    //    private String brand25;
    //    private String himok25;
    //    private String brand26;
    //    private String himok26;
    //    private String brand27;
    //    private String himok27;
    //    private String brand28;
    //    private String himok28;
    //    private String brand29;
    //    private String himok29;
    //    private String brand30;
    //    private String himok30;
    //    private String ftesury;
    //    private String bubkbn;
    //    private String sofkbn;
    //    private String hkiji;
    //    private String hseihin;
    //    private String hnefuda;
    //    private String hfuzoku;
    //    private String sofhou;
    //    private String dummy2;
    //    private String lokkbn;
    private String souflg;
    //    private String mntflg;
    //    private String tanto;
    //    private String crtymd;
    //    private String updymd;
    //    private String pgid;
    //    private String souflga;
    //    private String souymda;
    private String yugaikbn;
    //    private String yugaiymd;
}
