//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.component.model.PurchaseRecord;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ピッキングリストのModel.
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
@XmlRootElement(name = "purchase_record")
public class PurchaseRecordListXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ページ品質表示部. */
    @XmlElement(name = "page_details")
    private  List<PurchaseRecordDetailXmlModel> pageDetails;

}
//PRD_0133 #10181 add JFE start