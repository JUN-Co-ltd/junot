package jp.co.jun.edi.component.model.returnItem;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * 返品明細のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "return_item")
public class ReturnItemXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ヘッダ部. */
    @XmlElement(name = "page_head")
    private ReturnItemHeadXmlModel pageHead;

    /** ページ入荷情報部. */
    @XmlElement(name = "stock_info")
    private ReturnItemStockXmlModel pageStock;

    /** ページ品質表示部. */
    @XmlElement(name = "page_details")
    private  List<ReturnItemDetailXmlModel> pageDetails;

}
