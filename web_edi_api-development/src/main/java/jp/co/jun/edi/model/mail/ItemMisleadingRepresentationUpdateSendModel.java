package jp.co.jun.edi.model.mail;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 優良誤認更新時ののメール送信先用データModel.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemMisleadingRepresentationUpdateSendModel extends GetMailAdressCommonModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番ID. */
    private BigInteger partNoId;

    /** 品番. */
    private String partNo;

    /** 品名. */
    private String productName;

    /** 生産メーカーコード. */
    private String mdfMakerCode;

    /** 生産メーカー名. */
    private String mdfMakerName;

    /** 優良誤認承認区分（組成）. */
    private String qualityCompositionSstatusText;

    /** 優良誤認承認区分（国）. */
    private String qualityCooStatusText;

    /** 優良誤認承認区分（有害物質）. */
    private String qualityHarmfulStatusText;

    /** 備考リスト. */
    private List<String> memoList;

    /** URL. */
    private String url;

    /** 件名接頭辞. */
    private String subjectPrefix;
}

