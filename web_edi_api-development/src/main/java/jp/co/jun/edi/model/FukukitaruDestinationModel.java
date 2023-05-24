package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Convert;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.entity.converter.BooleanTypeConverter;
import jp.co.jun.edi.type.BooleanType;
import lombok.Data;

/**
 * フクキタル用宛先情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FukukitaruDestinationModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** ID. */
    private BigInteger id;

    /** 住所. */
    private String address;

    /** 会社名. */
    private String companyName;

    /** FAX番号. */
    private String fax;

    /** 郵便番号. */
    private String postalCode;

    /** 電話番号. */
    private String tel;

    /**
     * 承認需要フラグ.
     * false:承認不要(0)、true:承認必要 (1)
     */
    @Convert(converter = BooleanTypeConverter.class)
    private BooleanType isApprovalRequired;
}
