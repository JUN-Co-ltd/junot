package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * 店舗マスタのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JunpcTnpmstModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    private BigInteger id;

    /** 店舗コード. */
    private String shpcd;

    /** 店舗名称. */
    private String name;

    /** 店舗名略称. */
    private String sname;

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

    /** FAX番号:建屋代表. */
    private String faxban;

    /** 開店日 .*/
    private String opnymd;

    /** 閉店日 .*/
    private String clsymd;

    /** 配分課. */
    private String hka;

    /** 配分順. */
    private String hjun;

    /** 配分区分. */
    private AllocationType distrikind;

    /** 直送フラグ. */
    private BooleanType directDeliveryFlg;

    /** 物流コード. */
    private String logisticsCode;

    /** 場所コード. */
    private String allocationCode;

    // PRD_0041 add SIT start
    /** 倉庫区分. */
    private Integer warekind;
    // PRD_0041 add SIT end
}
