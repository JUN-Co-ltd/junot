package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jp.co.jun.edi.component.model.adapter.SpecialtyQubeDeleteStatusTypeAdapter;
import jp.co.jun.edi.component.model.adapter.SpecialtyQubeProcesstimeAdapter;
import jp.co.jun.edi.type.SpecialtyQubeDeleteStatusType;
import lombok.Data;

/**
 * 全店配分削除APIレスポンス情報のXmlModel.
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result")
public class SpecialtyQubeDeleteResponseXmlModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 処理時間. */
    @XmlJavaTypeAdapter(SpecialtyQubeProcesstimeAdapter.class)
    @XmlElement(name = "processtime")
    private LocalDateTime processtime;

    /** ステータス. */
    @XmlJavaTypeAdapter(SpecialtyQubeDeleteStatusTypeAdapter.class)
    @XmlElement(name = "status")
    private SpecialtyQubeDeleteStatusType status;

    /** エラーリスト. */
    @XmlElementWrapper(name = "error_list")
    @XmlElement(name = "error_cd")
    private List<String> errorList;
}
