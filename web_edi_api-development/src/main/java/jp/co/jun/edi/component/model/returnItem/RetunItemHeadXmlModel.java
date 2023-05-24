package jp.co.jun.edi.component.model.returnItem;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * データ部
 * ヘッダのModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
public class RetunItemHeadXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 合計. */
    @XmlElement(name = "total_amount")
    private String totalAmount = "合計";

    /** 小計. */
    @XmlElement(name = "subtotal")
    private String subtotal;

    /** ヘッダレコード. */
    @XmlElement(name = "head_record")
    private RetunItemHeadRecordXmlModel headRecord;
}
