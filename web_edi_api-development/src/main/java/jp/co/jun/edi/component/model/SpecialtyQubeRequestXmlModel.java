package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * SQリクエスト情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "request")
public class SpecialtyQubeRequestXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 発注番号(発注No). */
    @XmlElement(name = "ordernum")
    private BigInteger orderNum;

    /** 発注回数(納品依頼回数). */
    @XmlElement(name = "ordercount")
    private String orderCount;
}
