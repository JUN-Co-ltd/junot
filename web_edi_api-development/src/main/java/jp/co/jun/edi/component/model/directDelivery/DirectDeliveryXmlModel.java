package jp.co.jun.edi.component.model.directDelivery;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 納品出荷明細のModel.
 *
 * ※ メインモデル
 *
 * 【構成】
 * ・<b>XmlModel</b>
 *    L [page_details] : ページごとの表示情報
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "direct_delivery")
public class DirectDeliveryXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ページ品質表示部. */
    @XmlElement(name = "page_details")
    private  List<DirectDeliveryDetailXmlModel> pageDetails;

}
