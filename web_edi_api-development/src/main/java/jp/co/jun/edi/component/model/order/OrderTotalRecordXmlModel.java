package jp.co.jun.edi.component.model.order;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部
 * ページ明細_レコードセクション_合計レコードのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)

public class OrderTotalRecordXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 全体小計 文字列. */
    @XmlElement(name = "whole_subtotal_char")
    private String wholeSubtotalChar;

    /** 全体合計 文字列. */
    @XmlElement(name = "whole_total_amount_char")
    private String wholeTotalAmountChar;

    /** 全体小計/合計 値. */
    @XmlElement(name = "whole_subtotal_num")
    private String wholeSubtotalNum;

    /** サイズ別合計/小計. */
    @XmlElement(name = "size_subtotal")
    private List<String> sizeSubtotal;
}
