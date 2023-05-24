package jp.co.jun.edi.model.mail;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import jp.co.jun.edi.component.model.MaterialOrderLinkingErrorMailForwardCsvModel;
import lombok.Data;

/**
 * 資材発注連携エラーメール転送用メールデータモデル.
 */
@Data
public class MaterialOrderLinkingErrorMailForwardModel {
    /** メッセージ. */
    private List<MaterialOrderLinkingErrorMailForwardCsvModel> messages;
    /** 発注日. */
    private Date orderAt;
    /** オーダー識別コード. */
    private String orderCode;
    /** 発注No. */
    private BigInteger orderNo;
    /** 品番. */
    private String partNo;
    /** 希望出荷日. */
    private Date preferredShippingAt;
    /** 品名. */
    private String productName;
    /** 仕入コード. */
    private String sire;
    /** 品番ID. */
    private BigInteger partNoId;
    /** 発注ID. */
    private BigInteger orderId;
    /** 資材発注ID. */
    private BigInteger fOrderId;
    /** URL. */
    private String url;
    /** URI. */
    private String uri;
    /** メールタイトル接頭語. */
    private String subjectPrefix;
}
