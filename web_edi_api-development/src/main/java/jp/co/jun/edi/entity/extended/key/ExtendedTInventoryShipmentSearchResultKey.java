package jp.co.jun.edi.entity.extended.key;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import jp.co.jun.edi.entity.converter.InstructorSystemTypeConverter;
import jp.co.jun.edi.type.InstructorSystemType;
import lombok.Data;

/**
 * 在庫出荷情報のEntity.
 */
@Embeddable
@Data
public class ExtendedTInventoryShipmentSearchResultKey  implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出荷日. */
    @Column(name = "cargo_at")
    private Date cargoAt;

    /** 指示元システム. */
    @Column(name = "instructor_system")
    @Convert(converter = InstructorSystemTypeConverter.class)
    private InstructorSystemType instructorSystem;

    /** 課コード. */
    @Column(name = "division_code")
    private String divisionCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;
}
